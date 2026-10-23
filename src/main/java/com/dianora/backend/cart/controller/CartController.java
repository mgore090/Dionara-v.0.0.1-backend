package com.dianora.backend.cart.controller;

import com.dianora.backend.cart.dto.AddCartItemRequest;
import com.dianora.backend.cart.dto.CartResponse;
import com.dianora.backend.cart.dto.UpdateCartItemRequest;
import com.dianora.backend.cart.service.CartService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Create or retrieve a cart for testing.
    @PostMapping
    public ResponseEntity<CartResponse> createCart(
            @RequestParam Long userId) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cartService.getOrCreateCart(userId));
    }

    // Get cart.
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable Long userId) {

        return ResponseEntity.ok(cartService.getCart(userId));
    }

    // Add product to cart.
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Long userId,
            @Valid @RequestBody AddCartItemRequest request) {

        return ResponseEntity.ok(
                cartService.addItem(userId, request)
        );
    }

    // Update quantity.
    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        return ResponseEntity.ok(
                cartService.updateItem(userId, itemId, request)
        );
    }

    // Remove an item.
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long userId,
            @PathVariable Long itemId) {

        return ResponseEntity.ok(
                cartService.removeItem(userId, itemId)
        );
    }

    // Clear the cart.
    @DeleteMapping("/{userId}/items")
    public ResponseEntity<CartResponse> clearCart(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                cartService.clearCart(userId)
        );
    }
}