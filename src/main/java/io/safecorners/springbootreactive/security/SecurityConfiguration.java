package io.safecorners.springbootreactive.security;

import io.safecorners.springbootreactive.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Arrays;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    public static final String USER = "USER";
    public static final String INVENTORY = "INVENTORY";

    @Bean
    SecurityWebFilterChain customSecurityPolicy(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange -> exchange
                    .anyExchange().authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .formLogin()
                )
                .csrf().disable()
                .build();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByName(username)
                .map(user -> User.withDefaultPasswordEncoder()
                    .username(user.getName())
                    .password(user.getPassword())
                    .authorities(user.getRoles().toArray(new String[0])).build()
                );
    }

    @Bean
    CommandLineRunner userLoader(MongoOperations mongoOperations) {
        return args -> {
          mongoOperations.save(new io.safecorners.springbootreactive.domain.User(
                  "coffee", "coffee", Arrays.asList(role(USER))));

            mongoOperations.save(new io.safecorners.springbootreactive.domain.User(
                    "manager", "manager", Arrays.asList(role(USER), role(INVENTORY))));
        };
    }

    static String role(String auth) {
        return "ROLE_" + auth;
    }
}
