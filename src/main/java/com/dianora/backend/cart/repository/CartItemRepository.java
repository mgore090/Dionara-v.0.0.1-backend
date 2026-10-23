package com.dianora.backend.cart.repository;


import com.dianora.backend.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_IdAndProduct_Id(
            Long cartId,
            Long productId
    );

    Optional<CartItem> findByIdAndCart_Id(
            Long itemId,
            Long cartId
    );
}