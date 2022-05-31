package io.safecorners.springbootreactive.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import io.safecorners.springbootreactive.domain.Cart;

public interface CartRepository extends ReactiveCrudRepository<Cart, String> {
    
}
