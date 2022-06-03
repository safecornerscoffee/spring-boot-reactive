package io.safecorners.rsocketclient.controller;

import io.rsocket.metadata.WellKnownMimeType;
import io.safecorners.rsocketclient.domain.Item;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;
import static org.springframework.http.MediaType.parseMediaType;

@RestController
public class RSocketController {

    private final Mono<RSocketRequester> requester;

    public RSocketController(RSocketRequester.Builder builder) {
        this.requester = Mono.just(builder
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .metadataMimeType(parseMediaType(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.toString()))
                .rsocketConnector(connector ->
                        connector.reconnect(Retry.fixedDelay(5, Duration.ofSeconds(2))))
                .tcp("localhost", 7000));

    }


    @PostMapping("/items/request-response")
    Mono<ResponseEntity<?>> addNewItemViaRSocketRequestResponse(@RequestBody Item item) {
        return requester
                .flatMap(rSocketRequester -> rSocketRequester
                    .route("newItems.request-response")
                    .data(item)
                    .retrieveMono(Item.class))
                .map(savedItem -> ResponseEntity.created(URI.create("/items/request-response")).body(savedItem));
    }

    @GetMapping(value = "/items/request-stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    Flux<Item> findItemsUsingRsocketRequestStream() {
        return requester
                .flatMapMany(rSocketRequester -> rSocketRequester
                    .route("newItems.request-stream")
                    .retrieveFlux(Item.class)
                    .delayElements(Duration.ofSeconds(1)));
    }

    @PostMapping("/items/fire-and-forget")
    Mono<ResponseEntity<?>> addNewItemUsingRSocketFireAndForget(@RequestBody Item item) {
        return requester
                .flatMap(rSocketRequester -> rSocketRequester
                    .route("newItems.fire-and-forget")
                    .data(item)
                    .send())
                .then(Mono.just(ResponseEntity.created(URI.create("/items/fire-and-forget")).build()));
    }

    @GetMapping(value = "/items", produces = TEXT_EVENT_STREAM_VALUE)
    Flux<Item> liveUpdates() {
        return requester
                .flatMapMany(rSocketRequester -> rSocketRequester
                    .route("newItems.monitor")
                    .retrieveFlux(Item.class)
                );
    }
}
