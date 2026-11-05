//package com.dianora.backend.category.service;
//
//import com.dianora.backend.category.dto.CategoryRequest;
//import com.dianora.backend.category.dto.CategoryResponse;
//import com.dianora.backend.category.entity.Category;
//import com.dianora.backend.category.repository.CategoryRepository;
//import com.dianora.backend.product.repository.ProductRepository;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//
//@Service
//@Transactional
//public class CategoryService {
//
//    private final CategoryRepository categoryRepository;
//    private final ProductRepository productRepository;
//
//    public CategoryService(
//            CategoryRepository categoryRepository,
//            ProductRepository productRepository) {
//
//        this.categoryRepository = categoryRepository;
//        this.productRepository = productRepository;
//    }
//
//    // Create Category
//    public CategoryResponse createCategory(CategoryRequest request) {
//
//        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
//            throw new ResponseStatusException(
//                    HttpStatus.CONFLICT,
//                    "Category already exists"
//            );
//        }
//
//        Category category = new Category();
//        category.setName(request.name().trim());
//        category.setDescription(request.description());
//        category.setActive(true);
//
//        Category savedCategory = categoryRepository.save(category);
//
//        return mapToResponse(savedCategory);
//    }
//
//    // Get All Categories
//    @Transactional(readOnly = true)
//    public List<CategoryResponse> getAllCategories() {
//
//        return categoryRepository.findAll()
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }
//
//    // Get Category By ID
//    @Transactional(readOnly = true)
//    public CategoryResponse getCategoryById(Long id) {
//
//        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Category not found with id: " + id
//                ));
//
//        return mapToResponse(category);
//    }
//
//    // Update Category
//    public CategoryResponse updateCategory(
//            Long id,
//            CategoryRequest request) {
//
//        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Category not found with id: " + id
//                ));
//
//        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(
//                request.name().trim(), id)) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.CONFLICT,
//                    "Another category with this name already exists"
//            );
//        }
//
//        category.setName(request.name().trim());
//        category.setDescription(request.description());
//
//        Category updatedCategory = categoryRepository.save(category);
//
//        return mapToResponse(updatedCategory);
//    }
//
//    // Deactivate Category
//    public void deactivateCategory(Long id) {
//
//        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "Category not found with id: " + id
//                ));
//
//        if (productRepository.existsByCategory_Id(id)) {
//            throw new ResponseStatusException(
//                    HttpStatus.CONFLICT,
//                    "Cannot deactivate category because products are assigned to it"
//            );
//        }
//
//        category.setActive(false);
//        categoryRepository.save(category);
//    }
//
//    // Entity to Response DTO
//    private CategoryResponse mapToResponse(Category category) {
//
//        return new CategoryResponse(
//                category.getId(),
//                category.getName(),
//                category.getDescription(),
//                category.isActive(),
//                category.getProducts().size()
//        );
//    }
//}

package com.dianora.backend.category.service;

import com.dianora.backend.category.dto.CategoryRequest;
import com.dianora.backend.category.dto.CategoryResponse;
import com.dianora.backend.category.entity.Category;
import com.dianora.backend.category.repository.CategoryRepository;
import com.dianora.backend.product.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        String name = normalizeName(request.name());

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(normalizeDescription(request.description()));
        category.setActive(true);

        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return toResponse(findCategory(id));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findCategory(id);
        String name = normalizeName(request.name());

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category name already exists");
        }

        category.setName(name);
        category.setDescription(normalizeDescription(request.description()));

        return toResponse(categoryRepository.save(category));
    }

    public void deactivate(Long id) {
        Category category = findCategory(id);

        if (productRepository.existsByCategory_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot deactivate a category containing products");
        }

        category.setActive(false);
        categoryRepository.save(category);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getProducts().size()
        );
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Category name is required");
        }
        return name.trim();
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank()
                ? null : description.trim();
    }
}
