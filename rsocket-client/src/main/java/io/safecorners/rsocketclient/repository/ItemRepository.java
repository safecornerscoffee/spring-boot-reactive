package io.safecorners.rsocketclient.repository;

import io.safecorners.rsocketclient.domain.Item;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ItemRepository extends ReactiveCrudRepository<Item, String> {
}
