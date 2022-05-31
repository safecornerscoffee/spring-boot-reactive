package io.safecorners.springbootreactive.service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import io.safecorners.springbootreactive.domain.Dish;
import reactor.core.publisher.Flux;

@Service
public class KitchenService {

    private List<Dish> menu = Arrays.asList(
        new Dish("Sesame Chicken"),
        new Dish("Lo mein Noodles, plain"),
        new Dish("Sweet & Sour Beef")
    );

    public Flux<Dish> getDishes() {
        return Flux.<Dish>generate(sink -> sink.next(randomDish()))
        .delayElements(Duration.ofMillis(250));
    }

    private Dish randomDish() {
        return menu.get(picker.nextInt(menu.size()));
    }



    private Random picker = new Random();
}
