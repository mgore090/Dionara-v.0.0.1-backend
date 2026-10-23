package com.dianora.backend.product.repository;


import com.dianora.backend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrue();

    List<Product> findByCategory_IdAndActiveTrue(Long categoryId);

    boolean existsByCategory_Id(Long categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
