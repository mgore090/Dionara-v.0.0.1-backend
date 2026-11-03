//package com.dianora.backend.product.service;
//import com.dianora.backend.category.entity.Category;
//import com.dianora.backend.category.repository.CategoryRepository;
//import com.dianora.backend.product.dto.ProductRequest;
//import com.dianora.backend.product.dto.ProductResponse;
//import com.dianora.backend.product.entity.Product;
//import com.dianora.backend.product.repository.ProductRepository;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.server.ResponseStatusException;
//import com.dianora.backend.product.dto.UpdateStockRequest;
//
//import java.util.List;
//
//@Service
//@Transactional
//public class ProductService {
//
//    private final ProductRepository productRepository;
//    private final CategoryRepository categoryRepository;
//
//    public ProductService(
//            ProductRepository productRepository,
//            CategoryRepository categoryRepository
//    ) {
//        this.productRepository = productRepository;
//        this.categoryRepository = categoryRepository;
//    }
//
//    public ProductResponse create(ProductRequest request) {
//        validatePrices(request);
//
//        Category category = findActiveCategory(request.categoryId());
//
//        Product product = new Product(
//                request.name().trim(),
//                request.price(),
//                request.description(),
//                request.imageUrl(),
//                request.stock(),
//                category
//        );
//
//        product.setOriginalPrice(request.originalPrice());
//        product.setSize(request.size());
//        product.setImages(request.images());
//        product.setFeatures(request.features());
//
//        Product savedProduct = productRepository.save(product);
//
//        return toResponse(savedProduct);
//    }
//
//    @Transactional(readOnly = true)
//    public List<ProductResponse> getAll() {
//        return productRepository.findByActiveTrue()
//                .stream()
//                .map(this::toResponse)
//                .toList();
//    }
//
//    @Transactional(readOnly = true)
//    public ProductResponse getById(Long id) {
//        Product product = productRepository.findById(id)
//                .filter(Product::isActive)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Product not found: " + id
//                ));
//
//        return toResponse(product);
//    }
//
//    @Transactional(readOnly = true)
//    public List<ProductResponse> getByCategory(Long categoryId) {
//        findActiveCategory(categoryId);
//
//        return productRepository
//                .findByCategory_IdAndActiveTrue(categoryId)
//                .stream()
//                .map(this::toResponse)
//                .toList();
//    }
//
//    public ProductResponse update(
//            Long id,
//            ProductRequest request
//    ) {
//        validatePrices(request);
//
//        Product product = productRepository.findById(id)
//                .filter(Product::isActive)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Product not found: " + id
//                ));
//
//        Category category = findActiveCategory(request.categoryId());
//
//        product.setName(request.name().trim());
//        product.setPrice(request.price());
//        product.setOriginalPrice(request.originalPrice());
//        product.setDescription(request.description());
//        product.setImageUrl(request.imageUrl());
//        product.setStock(request.stock());
//        product.setSize(request.size());
//        product.setImages(request.images());
//        product.setFeatures(request.features());
//        product.setCategory(category);
//
//        Product updatedProduct = productRepository.save(product);
//
//        return toResponse(updatedProduct);
//    }
//
//    public void deactivate(Long id) {
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Product not found: " + id
//                ));
//
//        product.setActive(false);
//        productRepository.save(product);
//    }
//
//    private void validatePrices(ProductRequest request) {
//        if (request.originalPrice() != null
//                && request.price() != null
//                && request.originalPrice().compareTo(request.price()) < 0) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Original price cannot be lower than selling price"
//            );
//        }
//    }
//
//    private Category findActiveCategory(Long categoryId) {
//        return categoryRepository.findById(categoryId)
//                .filter(Category::isActive)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.BAD_REQUEST,
//                        "Category not found or inactive: " + categoryId
//                ));
//    }
//
//    private ProductResponse toResponse(Product product) {
//        return new ProductResponse(
//                product.getId(),
//                product.getName(),
//                product.getPrice(),
//                product.getDescription(),
//                product.getImageUrl(),
//                product.getStock(),
//                product.isActive(),
//                product.getCategory().getId(),
//                product.getCategory().getName(),
//                product.getOriginalPrice(),
//                List.copyOf(product.getImages()),
//                List.copyOf(product.getFeatures()),
//                product.getSize()
//        );
//    }
//
//
//    public ProductResponse updateStock(
//            Long id,
//            UpdateStockRequest request) {
//
//        Product product = productRepository.findById(id)
//                .filter(Product::isActive)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Active product not found: " + id
//                ));
//
//        if (request.operation() == null
//                || request.quantity() == null
//                || request.reason() == null
//                || request.reason().isBlank()) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Operation, quantity and reason are required"
//            );
//        }
//
//        String operation = request.operation().trim().toUpperCase();
//        int quantity = request.quantity();
//
//        if (quantity <= 0) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Quantity must be greater than zero"
//            );
//        }
//
//        if (!operation.equals("ADD") && !operation.equals("REMOVE")) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Operation must be ADD or REMOVE"
//            );
//        }
//
//        long newStock;
//
//        if (operation.equals("ADD")) {
//            newStock = (long) product.getStock() + quantity;
//        } else {
//            newStock = (long) product.getStock() - quantity;
//        }
//
//        if (newStock < 0) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Insufficient stock. Current stock is "
//                            + product.getStock()
//            );
//        }
//
//        if (newStock > Integer.MAX_VALUE) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Stock quantity is too large"
//            );
//        }
//
//        product.setStock((int) newStock);
//
//        Product savedProduct = productRepository.save(product);
//
//        return toResponse(savedProduct);
//    }
//
//
//}

package com.dianora.backend.product.service;

import com.dianora.backend.category.entity.Category;
import com.dianora.backend.category.repository.CategoryRepository;
import com.dianora.backend.product.dto.ProductRequest;
import com.dianora.backend.product.dto.ProductResponse;
import com.dianora.backend.product.dto.UpdateStockRequest;

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

        product.setOriginalPrice(request.originalPrice());
        product.setSize(request.size());
        List<String> imageList = normalizeImages(request.images(), request.imageUrl());
        product.setImages(imageList);
        product.setImageUrl(imageList.isEmpty() ? request.imageUrl() : imageList.get(0));
        product.setFeatures(request.features());
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
        List<String> imageList = normalizeImages(request.images(), request.imageUrl());
        product.setImages(imageList);
        product.setImageUrl(imageList.isEmpty() ? request.imageUrl() : imageList.get(0));
        product.setOriginalPrice(request.originalPrice());
        product.setFeatures(request.features());
        product.setSize(request.size());
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

    public ProductResponse updateStock(Long id, UpdateStockRequest request) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Active product not found: " + id));

        if (request.operation() == null || request.quantity() == null
                || request.reason() == null || request.reason().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Operation, quantity and reason are required");
        }

        String operation = request.operation().trim().toUpperCase();
        int quantity = request.quantity();
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero");
        }
        if (!operation.equals("ADD") && !operation.equals("REMOVE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Operation must be ADD or REMOVE");
        }

        long newStock = operation.equals("ADD")
                ? (long) product.getStock() + quantity
                : (long) product.getStock() - quantity;

        if (newStock < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Insufficient stock. Current stock is " + product.getStock());
        }
        if (newStock > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock quantity is too large");
        }

        product.setStock((int) newStock);
        return toResponse(productRepository.save(product));
    }

    public ProductResponse removeImage(Long productId, String imageUrl) {
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        List<String> current = new java.util.ArrayList<>(product.getImages());
        current.removeIf(image -> image.equals(imageUrl));
        product.setImages(current);
        product.setImageUrl(current.isEmpty() ? null : current.get(0));
        return toResponse(productRepository.save(product));
    }

    private List<String> normalizeImages(List<String> requestImages, String mainImage) {
        List<String> result = new java.util.ArrayList<>();
        if (mainImage != null && !mainImage.isBlank()) result.add(mainImage.trim());
        if (requestImages != null) {
            for (String image : requestImages) {
                if (image != null && !image.isBlank() && !result.contains(image.trim())) result.add(image.trim());
            }
        }
        if (result.size() > 10) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 10 images per product");
        return result;
    }

    private ProductResponse toResponse(Product product) {
        List<String> images = product.getImages() == null ? List.of() : List.copyOf(product.getImages());
        if (images.isEmpty() && product.getImageUrl() != null && !product.getImageUrl().isBlank()) images = List.of(product.getImageUrl());
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getImageUrl(),
                product.getStock(),
                product.isActive(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getOriginalPrice(),
                images,
                product.getFeatures() == null ? List.of() : List.copyOf(product.getFeatures()),
                product.getSize()
        );
    }
}
