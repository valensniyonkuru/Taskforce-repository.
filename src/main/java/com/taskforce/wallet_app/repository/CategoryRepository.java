package com.taskforce.wallet_app.repository;

import com.taskforce.wallet_app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find a category by name
    Category findByName(String name);
}
