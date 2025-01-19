package com.taskforce.wallet_app.controller;

import com.taskforce.wallet_app.model.Budget;
import com.taskforce.wallet_app.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "http://localhost:3000")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @PostMapping
    public ResponseEntity<Budget> createBudget(@Valid @RequestBody Budget budget) {
        return new ResponseEntity<>(budgetService.createBudget(budget), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@PathVariable Long id, 
                                             @Valid @RequestBody Budget budget) {
        return ResponseEntity.ok(budgetService.updateBudget(id, budget));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<Budget>> getActiveBudgets() {
        return ResponseEntity.ok(budgetService.getActiveBudgets());
    }

    @GetMapping("/exceeded")
    public ResponseEntity<List<Budget>> getBudgetsExceedingLimit() {
        return ResponseEntity.ok(budgetService.getBudgetsExceedingLimit());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Budget>> getBudgetsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(budgetService.getBudgetsByCategory(categoryId));
    }

    @GetMapping("/{id}/remaining")
    public ResponseEntity<Map<String, BigDecimal>> getRemainingBudget(@PathVariable Long id) {
        BigDecimal remaining = budgetService.calculateRemainingBudget(id);
        return ResponseEntity.ok(Map.of("remaining", remaining));
    }

    @PutMapping("/{id}/update-spending")
    public ResponseEntity<Void> updateBudgetSpending(@PathVariable Long id) {
        budgetService.updateBudgetSpending(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", e.getMessage()));
    }
}
