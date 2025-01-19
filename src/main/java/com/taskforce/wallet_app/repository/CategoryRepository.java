package com.taskforce.wallet_app.repository;

import com.taskforce.wallet_app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
    List<Category> findByParentCategoryIsNull();
    List<Category> findByParentCategory(Category parentCategory);
    List<Category> findByNameContainingIgnoreCase(String query);
    
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Transaction t WHERE t.category.id = :categoryId")
    boolean hasTransactions(@Param("categoryId") Long categoryId);
}
