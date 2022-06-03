package io.safecorners.springbootreactive.controller;

import io.safecorners.springbootreactive.domain.Item;
import io.safecorners.springbootreactive.service.InventoryService;
import org.springframework.security.core.Authentication;
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
    Mono<Rendering> home(Authentication auth) {
        return Mono.just(Rendering.view("home.html")
            .modelAttribute("items", inventoryService.getInventory())
            .modelAttribute("cart", inventoryService.getCart(cartName(auth)).defaultIfEmpty(new Cart(cartName(auth))))
            .modelAttribute("auth", auth)
            .build());
    }

    private String cartName(Authentication auth) {
        return auth.getName() + "'s Cart";
    }

    @PostMapping("/add/{id}")
    Mono<String> addToCart(Authentication auth, @PathVariable String id) {
        return inventoryService.addItemToCart(cartName(auth), id)
                .thenReturn("redirect:/");
    }

    @DeleteMapping("/remove/{id}")
    Mono<String> removeFromCart(Authentication auth, @PathVariable String id) {
        return inventoryService.removeItemFromCart(cartName(auth), id)
                .thenReturn("redirect:/");
    }

    @PostMapping("/")
    @ResponseBody
    Mono<Item> createItem(@RequestBody Item newItem) {
        return inventoryService.saveItem(newItem);
    }

    @DeleteMapping("/{id}")
    Mono<Void> deleteItem(@PathVariable String id) {
        return inventoryService.deleteItem(id);
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
