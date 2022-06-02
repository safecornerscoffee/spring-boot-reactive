package io.safecorners.springbootreactive.queue;

import io.safecorners.springbootreactive.domain.Item;
import io.safecorners.springbootreactive.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RabbitMQItemService {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQItemService.class);

    private final ItemRepository itemRepository;

    public RabbitMQItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @RabbitListener(
            ackMode = "MANUAL",
            bindings = @QueueBinding(
                    value = @Queue,
                    exchange = @Exchange("reactive"),
                    key = "new-item"
            )
    )
    public Mono<Void> processNewItem(Item item) {
        log.debug("Consuming => " + item);
        return itemRepository.save(item).then();
    }
}
