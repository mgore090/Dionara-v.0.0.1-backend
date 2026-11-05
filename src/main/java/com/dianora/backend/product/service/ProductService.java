package com.dianora.backend.product.service;

import com.dianora.backend.category.entity.Category;
import com.dianora.backend.category.repository.CategoryRepository;
import com.dianora.backend.product.dto.ProductRequest;
import com.dianora.backend.product.dto.ProductResponse;

import com.dianora.backend.product.entity.Product;
import com.dianora.backend.product.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse create(ProductRequest request) {
        Category category = findActiveCategory(request.categoryId());

        Product product = new Product(
                request.name().trim(),
                request.price(),
                request.description(),
                request.imageUrl(),
                request.stock(),
                category
        );

        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found: " + id
                ));

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getByCategory(Long categoryId) {
        findActiveCategory(categoryId);

        return productRepository
                .findByCategory_IdAndActiveTrue(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse update(
            Long id,
            ProductRequest request) {

        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found: " + id
                ));

        Category category = findActiveCategory(request.categoryId());

        product.setName(request.name().trim());
        product.setPrice(request.price());
        product.setDescription(request.description());
        product.setImageUrl(request.imageUrl());
        product.setStock(request.stock());
        product.setCategory(category);

        return toResponse(productRepository.save(product));
    }

    public void deactivate(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found: " + id
                ));

        product.setActive(false);
        productRepository.save(product);
    }

    private Category findActiveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .filter(Category::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Category not found or inactive: " + categoryId
                ));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getImageUrl(),
                product.getStock(),
                product.isActive(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}
