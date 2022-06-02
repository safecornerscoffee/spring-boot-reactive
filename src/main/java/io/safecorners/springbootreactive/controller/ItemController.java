package io.safecorners.springbootreactive.controller;

import io.safecorners.springbootreactive.domain.Item;
import io.safecorners.springbootreactive.repository.ItemRepository;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mediatype.alps.Alps;
import org.springframework.hateoas.mediatype.alps.Type;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mediatype.alps.Alps.alps;
import static org.springframework.hateoas.mediatype.alps.Alps.descriptor;

@RestController
@RequestMapping("/api")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    Mono<RepresentationModel<?>> root() {
        ItemController itemController = WebFluxLinkBuilder.methodOn(ItemController.class);

        Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(itemController.root()).withSelfRel().toMono();

        Mono<Link> itemsAggregateLink = WebFluxLinkBuilder.linkTo(itemController.findAll())
                .withRel(IanaLinkRelations.ITEM)
                .toMono();

        return selfLink.zipWith(itemsAggregateLink)
                .map(links -> Links.of(links.getT1(), links.getT2()))
                .map(links -> new RepresentationModel<>(links.toList()));
    }

    @GetMapping("/items")
    Mono<CollectionModel<EntityModel<Item>>> findAll() {
        ItemController itemController = WebFluxLinkBuilder.methodOn(ItemController.class);

        Mono<Link> aggregateRoot = WebFluxLinkBuilder.linkTo(itemController.findAll())
                .withSelfRel()
                .andAffordance(itemController.addItem(null))
                .toMono();

        return itemRepository.findAll()
                .flatMap(item -> findOne(item.getId()))
                .collectList()
                .flatMap(models -> aggregateRoot.map(selfLink -> CollectionModel.of(models, selfLink)));
    }

    @GetMapping("/items/{id}")
    Mono<EntityModel<Item>> findOne(@PathVariable String id) {
        ItemController itemController = WebFluxLinkBuilder.methodOn(ItemController.class);

        Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(itemController.findOne(id))
                .withSelfRel()
                .andAffordance(itemController.updateItem(null, id))
                .toMono();

        Mono<Link> aggregateLink = WebFluxLinkBuilder.linkTo(itemController.findAll())
                .withRel(IanaLinkRelations.ITEM)
                .toMono();

        return Mono.zip(itemRepository.findById(id), selfLink, aggregateLink)
                .map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(), o.getT3())));
    }

    @PostMapping("/items")
    Mono<ResponseEntity<?>> addItem(@RequestBody Mono<EntityModel<Item>> item) {
        return item
                .map(EntityModel::getContent)
                .flatMap(itemRepository::save)
                .map(Item::getId)
                .flatMap(this::findOne)
                .map(model -> ResponseEntity.created(model
                        .getRequiredLink(IanaLinkRelations.SELF)
                        .toUri())
                .body(model.getContent()));
    }

    @PutMapping("/items/{id}")
    public Mono<ResponseEntity<?>> updateItem(@RequestBody Mono<EntityModel<Item>> item, @PathVariable String id) {
        return item
                .map(EntityModel::getContent)
                .map(content -> new Item(id, content.getName(), content.getDescription(), content.getPrice()))
                .flatMap(itemRepository::save)
                .then(findOne(id))
                .map(model -> ResponseEntity.noContent().location(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .build());
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
