package io.safecorners.springbootreactive.controller;

import io.safecorners.springbootreactive.domain.CartItem;
import io.safecorners.springbootreactive.domain.Item;
import io.safecorners.springbootreactive.service.CartService;
import io.safecorners.springbootreactive.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;

import io.safecorners.springbootreactive.domain.Cart;
import io.safecorners.springbootreactive.repository.CartRepository;
import io.safecorners.springbootreactive.repository.ItemRepository;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {
    
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;

    public HomeController(ItemRepository itemRepository, CartRepository cartRepository,
                          CartService cartService, InventoryService inventoryService) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/")
    Mono<Rendering> home() {
        return Mono.just(Rendering.view("home.html")
            .modelAttribute("items", itemRepository.findAll())
            .modelAttribute("cart", cartRepository.findById("My Cart").defaultIfEmpty(new Cart("My Cart")))
            .build());
    }

    @PostMapping("/")
    Mono<String> createItem(@ModelAttribute Item newItem) {
        return itemRepository.save(newItem).thenReturn("redirect:/");
    }

    @DeleteMapping("/delete/{id}")
    Mono<String> deleteItem(@PathVariable String id) {
        return itemRepository.deleteById(id).thenReturn("redirect:/");
    }

    @PostMapping("/add/{id}")
    Mono<String> addToCart(@PathVariable String id) {
        return cartService.addToCart("My Cart", id)
                .thenReturn("redirect:/");
    }

    @GetMapping("/search")
    Mono<Rendering> search(@RequestParam(required = false) String name,
                           @RequestParam(required = false) String description,
                           @RequestParam boolean useAnd) {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("items", inventoryService.searchByExample(name, description, useAnd))
                .modelAttribute("cart", cartRepository.findById("My Cart").defaultIfEmpty(new Cart("My Cart")))
                .build());
    }
}
