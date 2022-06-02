package io.safecorners.springbootreactive.queue;

import io.safecorners.springbootreactive.domain.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

@RestController
@RequestMapping("/rabbit")
public class RabbitMQItemController {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQItemController.class);

    private final AmqpTemplate template;

    public RabbitMQItemController(AmqpTemplate template) {
        this.template = template;
    }

    @PostMapping("/items")
    Mono<ResponseEntity<?>> addItem(@RequestBody Mono<Item> item) {
        return item
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(content -> {
                    return Mono.fromCallable(() -> {
                        template.convertAndSend("reactive", "new-item", content);
                        return ResponseEntity.created(URI.create("/items")).build();
                    });
                });
    }

}
