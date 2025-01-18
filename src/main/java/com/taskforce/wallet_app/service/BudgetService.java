package com.taskforce.wallet_app.service;

import com.taskforce.wallet_app.model.Budget;
import com.taskforce.wallet_app.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    // Fetch all budgets
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    // Save a new budget
    public Budget saveBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    // Find budgets exceeding their limit
    public List<Budget> getBudgetsExceedingLimit() {
        return budgetRepository.findByCurrentSpentGreaterThan(0);
    }

    // Update an existing budget
    public Budget updateBudget(Long id, Budget updatedBudget) {
        return budgetRepository.findById(id)
                .map(budget -> {
                    budget.setLimit(updatedBudget.getLimit());
                    budget.setCurrentSpent(updatedBudget.getCurrentSpent());
                    return budgetRepository.save(budget);
                })
                .orElseThrow(() -> new RuntimeException("Budget not found with ID: " + id));
    }

    // Delete a budget by ID
    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }
}
