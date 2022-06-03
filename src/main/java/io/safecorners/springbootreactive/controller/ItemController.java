package io.safecorners.springbootreactive.controller;

import io.safecorners.springbootreactive.domain.Item;
import io.safecorners.springbootreactive.repository.ItemRepository;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mediatype.alps.Alps;
import org.springframework.hateoas.mediatype.alps.Type;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static io.safecorners.springbootreactive.security.SecurityConfiguration.INVENTORY;
import static org.springframework.hateoas.mediatype.alps.Alps.alps;
import static org.springframework.hateoas.mediatype.alps.Alps.descriptor;

@RestController
@RequestMapping("/api")
public class ItemController {

    private static final SimpleGrantedAuthority ROLE_INVENTORY = new SimpleGrantedAuthority("ROLE_" + INVENTORY);

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    Mono<RepresentationModel<?>> root() {
        ItemController itemController = WebFluxLinkBuilder.methodOn(ItemController.class);

        Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(itemController.root()).withSelfRel().toMono();

        Mono<Link> itemsAggregateLink = WebFluxLinkBuilder.linkTo(itemController.findAll(null))
                .withRel(IanaLinkRelations.ITEM)
                .toMono();

        return selfLink.zipWith(itemsAggregateLink)
                .map(links -> Links.of(links.getT1(), links.getT2()))
                .map(links -> new RepresentationModel<>(links.toList()));
    }

    @GetMapping("/items")
    Mono<CollectionModel<EntityModel<Item>>> findAll(Authentication auth) {
        ItemController itemController = WebFluxLinkBuilder.methodOn(ItemController.class);

        Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(itemController.findAll(auth))
                .withSelfRel().toMono();

        Mono<Links> allLinks = null;

        if (auth.getAuthorities().contains(ROLE_INVENTORY)) {
            Mono<Link> addLink = WebFluxLinkBuilder.linkTo(itemController.addItem(null, auth))
                    .withRel("add").toMono();

            allLinks = Mono.zip(selfLink, addLink)
                    .map(links -> Links.of(links.getT1(), links.getT2()));
        } else {
            allLinks = selfLink.map(Links::of);
        }

        return allLinks
                .flatMap(links -> itemRepository.findAll()
                    .flatMap(item -> findOne(item.getId(), auth))
                    .collectList()
                    .map(entityModels -> CollectionModel.of(entityModels, links))
                );
    }

    @GetMapping("/items/{id}")
    Mono<EntityModel<Item>> findOne(@PathVariable String id, Authentication auth) {
        ItemController itemController = WebFluxLinkBuilder.methodOn(ItemController.class);

        Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(itemController.findOne(id, auth))
                .withSelfRel().toMono();

        Mono<Link> aggregateLink = WebFluxLinkBuilder.linkTo(itemController.findAll(auth))
                .withRel(IanaLinkRelations.ITEM).toMono();

        Mono<Links> allLinks;

        if (auth.getAuthorities().contains(ROLE_INVENTORY)) {
            Mono<Link> deleteLink = WebFluxLinkBuilder.linkTo(itemController.deleteItem(id))
                    .withRel("delete").toMono();

            allLinks = Mono.zip(selfLink, aggregateLink, deleteLink)
                    .map(links -> Links.of(links.getT1(), links.getT2(), links.getT3()));
        } else {
            allLinks = Mono.zip(selfLink, aggregateLink)
                    .map(links -> Links.of(links.getT1(), links.getT2()));
        }

        return itemRepository.findById(id)
                .zipWith(allLinks)
                .map(o -> EntityModel.of(o.getT1(), o.getT2()));
    }

    @PreAuthorize("hasRole('" + INVENTORY  + "')")
    @PostMapping("/items")
    Mono<ResponseEntity<?>> addItem(@RequestBody Mono<EntityModel<Item>> item, Authentication auth) {
        return item
                .map(EntityModel::getContent)
                .flatMap(itemRepository::save)
                .map(Item::getId)
                .flatMap(id -> findOne(id, auth))
                .map(model -> ResponseEntity.created(model
                        .getRequiredLink(IanaLinkRelations.SELF)
                        .toUri())
                .body(model.getContent()));
    }

    @PutMapping("/items/{id}")
    public Mono<ResponseEntity<?>> updateItem(@RequestBody Mono<EntityModel<Item>> item, @PathVariable String id,
                                              Authentication auth) {
        return item
                .map(EntityModel::getContent)
                .map(content -> new Item(id, content.getName(), content.getDescription(), content.getPrice()))
                .flatMap(itemRepository::save)
                .then(findOne(id, auth))
                .map(model -> ResponseEntity.noContent().location(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .build());
    }

    @PreAuthorize("hasRole('" + INVENTORY + "')")
    @DeleteMapping("/items/{id}")
    Mono<ResponseEntity<?>> deleteItem(@PathVariable String id) {
        return itemRepository.deleteById(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping(value = "/items/profile", produces = MediaTypes.ALPS_JSON_VALUE)
    public Alps profile() {
        return alps()
                .descriptor(Collections.singletonList(
                        descriptor().id(Item.class.getSimpleName() + "-representation")
                        .descriptor(
                                Arrays.stream(Item.class.getDeclaredFields())
                                    .map(field -> descriptor().name(field.getName()).type(Type.SEMANTIC).build())
                                    .collect(Collectors.toList())
                        ).build()))
                .build();
    }
}
