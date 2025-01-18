package com.taskforce.wallet_app.controller;

import com.taskforce.wallet_app.model.Budget;
import com.taskforce.wallet_app.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // Get all budgets
    @GetMapping
    public List<Budget> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    // Create a new budget
    @PostMapping
    public ResponseEntity<Budget> createBudget(@RequestBody Budget budget) {
        Budget savedBudget = budgetService.saveBudget(budget);
        return ResponseEntity.ok(savedBudget);
    }

    // Find budgets exceeding their limit
    @GetMapping("/exceeding")
    public List<Budget> getBudgetsExceedingLimit() {
        return budgetService.getBudgetsExceedingLimit();
    }

    // Update an existing budget
    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@PathVariable Long id, @RequestBody Budget budget) {
        Budget updatedBudget = budgetService.updateBudget(id, budget);
        return ResponseEntity.ok(updatedBudget);
    }

    // Delete a budget
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
