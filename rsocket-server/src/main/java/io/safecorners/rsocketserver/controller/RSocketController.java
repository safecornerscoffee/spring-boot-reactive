package io.safecorners.rsocketserver.controller;

import io.safecorners.rsocketserver.domain.Item;
import io.safecorners.rsocketserver.repository.ItemRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.*;

@Controller
public class RSocketController {

    private final ItemRepository itemRepository;
    private final Sinks.Many<Item> itemsSink;

    public RSocketController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        this.itemsSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @MessageMapping("newItems.request-response")
    public Mono<Item> processNewItemsViaRSocketRequestResponse(Item item) {
        return itemRepository.save(item)
                .doOnNext(itemsSink::tryEmitNext);
    }

    @MessageMapping("newItems.request-stream")
    public Flux<Item> findItemsViaRSocketRequestStream() {
        return itemRepository.findAll()
                .doOnNext(itemsSink::tryEmitNext);
    }

    @MessageMapping("newItems.fire-and-forget")
    public Mono<Void> processNewItemsViaRSocketFireAndForget(Item item) {
        return itemRepository.save(item)
                .doOnNext(itemsSink::tryEmitNext)
                .then();
    }

    @MessageMapping("newItems.monitor")
    public Flux<Item> monitorNewItems() {
        return itemsSink.asFlux();
    }
}
