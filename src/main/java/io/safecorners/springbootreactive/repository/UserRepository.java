package io.safecorners.springbootreactive.repository;

import io.safecorners.springbootreactive.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, String> {
    Mono<User> findByName(String name);
}
