package com.taskforce.wallet_app.service;

import com.taskforce.wallet_app.model.Category;
import com.taskforce.wallet_app.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }

    public List<Category> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    public List<Category> getSubcategories(Long parentId) {
        Category parentCategory = getCategoryById(parentId);
        return categoryRepository.findByParentCategory(parentCategory);
    }

    @Transactional
    public Category createCategory(Category category) {
        validateCategory(category);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        validateCategoryUpdate(id, categoryDetails);

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setParentCategory(categoryDetails.getParentCategory());
        category.setColor(categoryDetails.getColor());
        category.setIcon(categoryDetails.getIcon());

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        
        // Check if category has subcategories
        if (!category.getSubCategories().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories");
        }
        
        // Check if category is used in any transactions
        if (categoryRepository.hasTransactions(id)) {
            throw new IllegalStateException("Cannot delete category that is used in transactions");
        }
        
        categoryRepository.delete(category);
    }

    public List<Category> searchCategories(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return categoryRepository.findByNameContainingIgnoreCase(query.trim());
    }

    public List<Category> getCategoryHierarchy(Long categoryId) {
        Category category = getCategoryById(categoryId);
        List<Category> hierarchy = new ArrayList<>();
        buildHierarchy(category, hierarchy);
        return hierarchy;
    }

    public List<Category> getCategoryPath(Long categoryId) {
        Category category = getCategoryById(categoryId);
        List<Category> path = new ArrayList<>();
        Category current = category;
        
        while (current != null) {
            path.add(0, current);
            current = current.getParentCategory();
        }
        
        return path;
    }

    public boolean isValidParentCategory(Long categoryId, Long parentId) {
        if (categoryId.equals(parentId)) {
            return false;
        }

        Category category = getCategoryById(categoryId);
        Category parentCategory = getCategoryById(parentId);

        // Check if parent is not a descendant of the category
        return !isDescendant(category, parentCategory);
    }

    private void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        // Check parent category if specified
        if (category.getParentCategory() != null) {
            Long parentId = category.getParentCategory().getId();
            if (parentId != null) {
                Category parentCategory = getCategoryById(parentId);
                category.setParentCategory(parentCategory);
                
                // Validate parent-child relationship
                if (!isValidParentCategory(category.getId(), parentId)) {
                    throw new IllegalArgumentException("Invalid parent category relationship");
                }
            }
        }

        // Check for duplicate names under the same parent
        Category parent = category.getParentCategory();
        List<Category> siblings = parent != null ? 
            categoryRepository.findByParentCategory(parent) : 
            categoryRepository.findByParentCategoryIsNull();
        
        boolean hasDuplicate = siblings.stream()
            .filter(sibling -> !sibling.getId().equals(category.getId()))
            .anyMatch(sibling -> sibling.getName().equalsIgnoreCase(category.getName().trim()));
        
        if (hasDuplicate) {
            throw new IllegalArgumentException("Category name already exists under the same parent");
        }
    }

    private void validateCategoryUpdate(Long id, Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        // Prevent circular references
        if (category.getParentCategory() != null) {
            Long parentId = category.getParentCategory().getId();
            if (parentId != null) {
                if (parentId.equals(id)) {
                    throw new IllegalArgumentException("Category cannot be its own parent");
                }
                
                Category parentCategory = getCategoryById(parentId);
                if (isDescendant(getCategoryById(id), parentCategory)) {
                    throw new IllegalArgumentException("Cannot set a descendant as parent category");
                }
            }
        }
    }

    private boolean isDescendant(Category ancestor, Category descendant) {
        if (descendant == null) {
            return false;
        }
        
        Category parent = descendant.getParentCategory();
        while (parent != null) {
            if (parent.getId().equals(ancestor.getId())) {
                return true;
            }
            parent = parent.getParentCategory();
        }
        
        return false;
    }

    private void buildHierarchy(Category category, List<Category> hierarchy) {
        if (category == null) {
            return;
        }

        hierarchy.add(category);
        if (category.getSubCategories() != null) {
            category.getSubCategories().forEach(subcategory -> buildHierarchy(subcategory, hierarchy));
        }
    }
}
