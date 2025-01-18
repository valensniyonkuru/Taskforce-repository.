package com.taskforce.wallet_app.repository;

import com.taskforce.wallet_app.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Import the List interface

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Custom method to find budgets exceeding their limit
    List<Budget> findByCurrentSpentGreaterThan(double limit);
}
