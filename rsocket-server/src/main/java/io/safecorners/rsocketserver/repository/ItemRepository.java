package io.safecorners.rsocketserver.repository;

import io.safecorners.rsocketserver.domain.Item;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ItemRepository extends ReactiveCrudRepository<Item, String> {
}
