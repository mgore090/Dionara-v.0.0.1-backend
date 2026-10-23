package com.dianora.backend.cart.service;

import com.dianora.backend.cart.dto.AddCartItemRequest;
import com.dianora.backend.cart.dto.CartItemResponse;
import com.dianora.backend.cart.dto.CartResponse;
import com.dianora.backend.cart.dto.UpdateCartItemRequest;
import com.dianora.backend.cart.entity.Cart;
import com.dianora.backend.cart.entity.CartItem;
import com.dianora.backend.cart.repository.CartItemRepository;
import com.dianora.backend.cart.repository.CartRepository;
import com.dianora.backend.product.entity.Product;
import com.dianora.backend.product.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    // Create a cart if one does not already exist.
    public CartResponse getOrCreateCart(Long userId) {

        validateUserId(userId);

        Cart cart = cartRepository.findByUserIdAndActiveTrue(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));

        return mapToCartResponse(cart);
    }

    // Get an existing cart.
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {

        validateUserId(userId);

        Cart cart = findCartByUserId(userId);
        return mapToCartResponse(cart);
    }

    // Add product to cart. If it already exists, increase its quantity.
    public CartResponse addItem(
            Long userId,
            AddCartItemRequest request) {

        validateUserId(userId);

        Cart cart = cartRepository.findByUserIdAndActiveTrue(userId)
                .orElseGet(() -> cartRepository.save(new Cart(userId)));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found: " + request.productId()
                ));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This product is not available"
            );
        }

        int requestedQuantity = request.quantity();

        if (requestedQuantity > product.getStock()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Requested quantity exceeds available stock"
            );
        }

        CartItem item = cartItemRepository
                .findByCart_IdAndProduct_Id(cart.getId(), product.getId())
                .orElse(null);

        if (item == null) {
            item = new CartItem(
                    product,
                    requestedQuantity,
                    product.getPrice()
            );

            cart.addItem(item);
        } else {
            int newQuantity = item.getQuantity() + requestedQuantity;

            if (newQuantity > product.getStock()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Total quantity exceeds available stock"
                );
            }

            item.setQuantity(newQuantity);
        }

        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    // Update quantity for an existing cart item.
    public CartResponse updateItem(
            Long userId,
            Long itemId,
            UpdateCartItemRequest request) {

        validateUserId(userId);

        Cart cart = findCartByUserId(userId);

        CartItem item = cartItemRepository
                .findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found: " + itemId
                ));

        Product product = productRepository.findById(
                        item.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product no longer exists"
                ));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This product is not available"
            );
        }

        if (request.quantity() > product.getStock()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Requested quantity exceeds available stock"
            );
        }

        item.setQuantity(request.quantity());

        // Update snapshot to current product price.
        item.setUnitPrice(product.getPrice());

        cartItemRepository.save(item);

        return mapToCartResponse(cart);
    }

    // Remove one item from the cart.
    public CartResponse removeItem(Long userId, Long itemId) {

        validateUserId(userId);

        Cart cart = findCartByUserId(userId);

        CartItem item = cartItemRepository
                .findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found: " + itemId
                ));

        cart.removeItem(item);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    // Empty the cart.
    public CartResponse clearCart(Long userId) {

        validateUserId(userId);

        Cart cart = findCartByUserId(userId);

        for (CartItem item : List.copyOf(cart.getItems())) {
            cart.removeItem(item);
        }

        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    private Cart findCartByUserId(Long userId) {

        return cartRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found for user: " + userId
                ));
    }

    private void validateUserId(Long userId) {

        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid user ID is required"
            );
        }
    }

    private CartResponse mapToCartResponse(Cart cart) {

        List<CartItemResponse> itemResponses = cart.getItems()
                .stream()
                .map(this::mapToCartItemResponse)
                .toList();

        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                itemResponses,
                totalItems,
                totalAmount
        );
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {

        Product product = item.getProduct();

        BigDecimal subtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                item.getUnitPrice(),
                item.getQuantity(),
                subtotal
        );
    }
}
