package io.safecorners.springbootreactive.controller;

import io.safecorners.springbootreactive.domain.Item;
import io.safecorners.springbootreactive.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaWebTestClientConfigurer;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@AutoConfigureWebTestClient
public class ItemControllerAuthorizationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    HypermediaWebTestClientConfigurer webTestClientConfigurer;

    @Autowired
    ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        webTestClient = this.webTestClient.mutateWith(webTestClientConfigurer);
    }

    @Test
    @WithMockUser(username = "alice", roles = {"SOME_OTHER_ROLE"})
    void addingInventoryWithoutProperRoleFails() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        webTestClient.post().uri("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(
                        new Item("iPhone X", "upgrade", 999.99)
                ))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "bob", roles = {"INVENTORY"})
    void addingInventoryWithProperRoleSucceeds() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        webTestClient.post().uri("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(
                        new Item("iPhone X", "upgrade", 999.99)
                ))
                .exchange()
                .expectStatus().isCreated();

        itemRepository.findByName("iPhone X")
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getDescription()).isEqualTo("upgrade");
                    assertThat(item.getPrice()).isEqualTo(999.99);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @WithMockUser(username = "carol", roles = {"SOME_OTHER_ROLE"})
    void deletingInventoryWithoutProperRoleFails() {
        webTestClient.delete().uri("/api/items/some-item")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username= "greg", roles = {"INVENTORY"})
    void deletingInventoryWithProperRoleSucceeds() {
        String id = itemRepository.save(new Item("Banana", "rocket", 22.22))
                .map(Item::getId)
                .block();

        webTestClient.delete().uri("/api/items/" + id)
                .exchange()
                .expectStatus().isNoContent();

        itemRepository.findById(id)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @WithMockUser(username = "alice", roles = {"INVENTORY"})
    void navigateToItemWithInventoryAuthority() {

        RepresentationModel<?> root = webTestClient.get().uri("/api")
                .exchange()
                .expectBody(RepresentationModel.class)
                .returnResult().getResponseBody();

        CollectionModel<EntityModel<Item>> items = webTestClient.get()
                .uri(root.getRequiredLink(IanaLinkRelations.ITEM).toUri())
                .exchange()
                .expectBody(new TypeReferences.CollectionModelType<EntityModel<Item>>() {})
                .returnResult().getResponseBody();

        assertThat(items.getLinks()).hasSize(2);
        assertThat(items.hasLink(IanaLinkRelations.SELF)).isTrue();
        assertThat(items.hasLink("add")).isTrue();

        EntityModel<Item> first = items.getContent().iterator().next();

        EntityModel<Item> item = webTestClient.get()
                .uri(first.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .exchange()
                .expectBody(new TypeReferences.EntityModelType<Item>() {})
                .returnResult().getResponseBody();

        assertThat(item.getLinks()).hasSize(3);
        assertThat(item.hasLink(IanaLinkRelations.SELF)).isTrue();
        assertThat(item.hasLink(IanaLinkRelations.ITEM)).isTrue();
        assertThat(item.hasLink("delete")).isTrue();
    }
}