package com.taskforce.wallet_app.controller;

import com.taskforce.wallet_app.model.Category;
import com.taskforce.wallet_app.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, 
                                                 @Valid @RequestBody Category category) {
        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<Category>> getSubcategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getSubcategories(parentId));
    }

    @GetMapping("/root")
    public ResponseEntity<List<Category>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @GetMapping("/{id}/hierarchy")
    public ResponseEntity<List<Category>> getCategoryHierarchy(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryHierarchy(id));
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<List<Category>> getCategoryPath(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryPath(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String query) {
        return ResponseEntity.ok(categoryService.searchCategories(query));
    }

    @GetMapping("/{id}/validate-parent/{parentId}")
    public ResponseEntity<Map<String, Boolean>> validateParentCategory(
            @PathVariable Long id, 
            @PathVariable Long parentId) {
        boolean isValid = categoryService.isValidParentCategory(id, parentId);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", e.getMessage()));
    }
}
