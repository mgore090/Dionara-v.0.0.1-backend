//package com.dianora.backend.product.controller;
//
//import com.dianora.backend.product.dto.ProductRequest;
//import com.dianora.backend.product.dto.ProductResponse;
//
//import com.dianora.backend.product.dto.UpdateStockRequest;
//import com.dianora.backend.product.service.ProductService;
//import jakarta.validation.Valid;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.UUID;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.server.ResponseStatusException;
//
//@RestController
//@RequestMapping("/api/products")
//public class ProductController {
//
//    private final ProductService productService;
//
//    @Value("${dianora.upload-dir:uploads/products}")
//    private String uploadDirectory;
//
//    public ProductController(ProductService productService) {
//        this.productService = productService;
//    }
//
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public ProductResponse create(
//            @Valid @RequestBody ProductRequest request) {
//        return productService.create(request);
//    }
//
//    @GetMapping
//    public List<ProductResponse> getAll() {
//        return productService.getAll();
//    }
//
//    @GetMapping("/{id}")
//    public ProductResponse getById(@PathVariable("id") Long id) {
//        return productService.getById(id);
//    }
//
//    @GetMapping("/category/{categoryId}")
//    public List<ProductResponse> getByCategory(
//            @PathVariable("categoryId") Long categoryId) {
//        return productService.getByCategory(categoryId);
//    }
//
//    @PutMapping("/{id}")
//    public ProductResponse update(
//            @PathVariable("id") Long id,
//            @Valid @RequestBody ProductRequest request) {
//        return productService.update(id, request);
//    }
//
//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void deactivate(@PathVariable("id") Long id) {
//        productService.deactivate(id);
//    }
//
//    @PatchMapping("/{id}/stock")
//    public ProductResponse updateStock(
//            @PathVariable("id") Long id,
//            @RequestBody UpdateStockRequest request) {
//        return productService.updateStock(id, request);
//    }
//
//
//
//    @PostMapping(value = "/upload-image", consumes = "multipart/form-data")
//    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
//        if (file == null || file.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select an image.");
//        }
//
//        String contentType = file.getContentType();
//        if (contentType == null || !(contentType.equals("image/jpeg")
//                || contentType.equals("image/png")
//                || contentType.equals("image/webp"))) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    "Only JPG, PNG, and WebP images are allowed.");
//        }
//
//        if (file.getSize() > 5 * 1024 * 1024) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    "Image size must be 5 MB or less.");
//        }
//
//        try {
//            Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
//            Files.createDirectories(uploadPath);
//
//            String originalName = file.getOriginalFilename() == null
//                    ? "product-image"
//                    : file.getOriginalFilename();
//            String extension = originalName.lastIndexOf('.') >= 0
//                    ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase()
//                    : ".jpg";
//            if (!extension.matches("\\.(jpg|jpeg|png|webp)")) {
//                extension = ".jpg";
//            }
//
//            String fileName = UUID.randomUUID() + extension;
//            Path destination = uploadPath.resolve(fileName).normalize();
//            if (!destination.startsWith(uploadPath)) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name.");
//            }
//
//            file.transferTo(destination);
//            return Map.of("imageUrl", "/uploads/products/" + fileName);
//        } catch (IOException ex) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Unable to save product image.", ex);
//        }
//    }
//}


package com.dianora.backend.product.controller;

import com.dianora.backend.product.dto.ProductRequest;
import com.dianora.backend.product.dto.ProductResponse;

import com.dianora.backend.product.dto.UpdateStockRequest;
import com.dianora.backend.product.service.ProductService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private static final long MAX_IMAGE_SIZE = 20 * 1024 * 1024;
    
    @Value("${dianora.upload-dir:uploads/products}")
    private String uploadDirectory;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable("id") Long id) {
        return productService.getById(id);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductResponse> getByCategory(
            @PathVariable("categoryId") Long categoryId) {
        return productService.getByCategory(categoryId);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable("id") Long id) {
        productService.deactivate(id);
    }

    @PatchMapping("/{id}/stock")
    public ProductResponse updateStock(
            @PathVariable("id") Long id,
            @RequestBody UpdateStockRequest request) {
        return productService.updateStock(id, request);
    }



    @DeleteMapping("/{id}/images")
    public ProductResponse removeImage(@PathVariable("id") Long id, @RequestParam("imageUrl") String imageUrl) {
        ProductResponse updated = productService.removeImage(id, imageUrl);
        deleteStoredImage(imageUrl);
        return updated;
    }

    private void deleteStoredImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/products/")) return;
        try {
            Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
            Path imagePath = uploadPath.resolve(imageUrl.substring("/uploads/products/".length())).normalize();
            if (imagePath.startsWith(uploadPath)) Files.deleteIfExists(imagePath);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete product image", ex);
        }
    }

    @PostMapping(value = "/upload-image", consumes = "multipart/form-data")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select an image.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only JPG, PNG, and WebP images are allowed.");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Image size must be 20 MB or less.");
        }

        try {
            Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename() == null
                    ? "product-image"
                    : file.getOriginalFilename();
            String extension = originalName.lastIndexOf('.') >= 0
                    ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase()
                    : ".jpg";
            if (!extension.matches("\\.(jpg|jpeg|png|webp)")) {
                extension = ".jpg";
            }

            String fileName = UUID.randomUUID() + extension;
            Path destination = uploadPath.resolve(fileName).normalize();
            if (!destination.startsWith(uploadPath)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name.");
            }

            file.transferTo(destination);
            return Map.of("imageUrl", "/uploads/products/" + fileName);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to save product image.", ex);
        }
    }
}