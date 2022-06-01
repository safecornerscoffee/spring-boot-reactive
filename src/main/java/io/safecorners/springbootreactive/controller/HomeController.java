package io.safecorners.springbootreactive.controller;

import io.safecorners.springbootreactive.domain.Item;
import io.safecorners.springbootreactive.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;

import io.safecorners.springbootreactive.domain.Cart;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {

    private final InventoryService inventoryService;

    public HomeController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/")
    Mono<Rendering> home() {
        return Mono.just(Rendering.view("home.html")
            .modelAttribute("items", inventoryService.getInventory())
            .modelAttribute("cart", inventoryService.getCart("My Cart").defaultIfEmpty(new Cart("My Cart")))
            .build());
    }

    @PostMapping("/add/{id}")
    Mono<String> addToCart(@PathVariable String id) {
        return inventoryService.addItemToCart("My Cart", id)
                .thenReturn("redirect:/");
    }

    @DeleteMapping("/remove/{id}")
    Mono<String> removeFromCart(@PathVariable String id) {
        return inventoryService.removeItemFromCart("My Cart", id)
                .thenReturn("redirect:/");
    }

    @PostMapping("/")
    Mono<String> createItem(@ModelAttribute Item newItem) {
        return inventoryService.saveItem(newItem)
                .thenReturn("redirect:/");
    }

    @DeleteMapping("/delete/{id}")
    Mono<String> deleteItem(@PathVariable String id) {
        return inventoryService.deleteItem(id)
                .thenReturn("redirect:/");
    }

    @GetMapping("/search")
    Mono<Rendering> search(@RequestParam(required = false) String name,
                           @RequestParam(required = false) String description,
                           @RequestParam boolean useAnd) {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("items", inventoryService.searchByExample(name, description, useAnd))
                .modelAttribute("cart", inventoryService.getCart("My Cart").defaultIfEmpty(new Cart("My Cart")))
                .build());
    }
}
