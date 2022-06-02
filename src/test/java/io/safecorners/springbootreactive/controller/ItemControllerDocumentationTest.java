package io.safecorners.springbootreactive.controller;

import io.safecorners.springbootreactive.domain.Item;
import io.safecorners.springbootreactive.repository.ItemRepository;
import io.safecorners.springbootreactive.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@WebFluxTest(ItemController.class)
@AutoConfigureRestDocs
public class ItemControllerDocumentationTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    InventoryService inventoryService;

    @MockBean
    ItemRepository itemRepository;

    @Test
    void findingAllItems() {
        when(itemRepository.findAll()).thenReturn(
                Flux.just(new Item("item-1", "Alf alarm clock",
                        "nothing I really need", 19.99)));

        when(itemRepository.findById(anyString())).thenReturn(
                Mono.just(new Item("item-1", "Alf alarm clock",
                        "nothing I really need", 19.99)));

        webTestClient.get().uri("/api/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("findAll", preprocessResponse(prettyPrint())));
    }

    @Test
    void postNewItem() {
        when(itemRepository.save(any())).thenReturn(
                Mono.just(new Item("1", "Alf alarm clock", "nothing important", 19.90)));

        when(itemRepository.findById(anyString())).thenReturn(
                Mono.just(new Item("1", "Alf alarm clock", "nothing important", 19.90)));

        webTestClient.post().uri("/api/items")
                .bodyValue(new Item("Alf alarm clock", "nothing important", 19.90))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .consumeWith(document("post-new-item", preprocessResponse(prettyPrint())));
    }

    @Test
    void findOneItem() {
        when(itemRepository.findById("item-1")).thenReturn(
                Mono.just(new Item("item-1", "Alf alarm clock", "nothing I really need", 19.99)));

        this.webTestClient.get().uri("/api/items/item-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("findOne", preprocessResponse(prettyPrint()),
                        links(
                                linkWithRel("self").description("Canonical link to this `Item`"),
                                linkWithRel("item").description("Link back to the aggregate root"))));
    }

    @Test
    void updateItem() {
        when(itemRepository.save(any())).thenReturn(
                Mono.just(new Item("1", "Alf alarm clock", "updated", 19.99)));

        when(itemRepository.findById(anyString())).thenReturn(
                Mono.just(new Item("1", "Alf alarm clock", "updated", 19.99)));

        this.webTestClient.put().uri("/api/items/1")
                .bodyValue(new Item("Alf alarm clock", "updated", 19.99))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .consumeWith(document("update-item", preprocessResponse(prettyPrint())))
                .isEmpty();
    }
}