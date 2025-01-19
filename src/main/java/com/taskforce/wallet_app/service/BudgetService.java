package com.taskforce.wallet_app.service;

import com.taskforce.wallet_app.model.Budget;
import com.taskforce.wallet_app.model.Category;
import com.taskforce.wallet_app.model.Transaction;
import com.taskforce.wallet_app.repository.BudgetRepository;
import com.taskforce.wallet_app.repository.CategoryRepository;
import com.taskforce.wallet_app.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository, 
                        CategoryRepository categoryRepository,
                        TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Budget not found with id: " + id));
    }

    public List<Budget> getActiveBudgets() {
        LocalDate now = LocalDate.now();
        return budgetRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now);
    }

    public List<Budget> getBudgetsExceedingLimit() {
        return budgetRepository.findBudgetsExceedingLimit();
    }

    @Transactional
    public Budget createBudget(Budget budget) {
        validateBudget(budget);
        
        // Set initial spent amount to 0
        budget.setSpentAmount(BigDecimal.ZERO);
        
        // Calculate current spending if budget period includes past dates
        if (budget.getStartDate().isBefore(LocalDate.now())) {
            updateBudgetSpending(budget);
        }
        
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget updateBudget(Long id, Budget budgetDetails) {
        Budget budget = getBudgetById(id);
        validateBudget(budgetDetails);

        budget.setName(budgetDetails.getName());
        budget.setBudgetLimit(budgetDetails.getBudgetLimit());
        budget.setStartDate(budgetDetails.getStartDate());
        budget.setEndDate(budgetDetails.getEndDate());
        budget.setCategory(budgetDetails.getCategory());
        
        // Recalculate spending if date range changed
        updateBudgetSpending(budget);
        
        return budgetRepository.save(budget);
    }

    @Transactional
    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new EntityNotFoundException("Budget not found with id: " + id);
        }
        budgetRepository.deleteById(id);
    }

    @Transactional
    public void updateBudgetSpending(Long budgetId) {
        Budget budget = getBudgetById(budgetId);
        updateBudgetSpending(budget);
        budgetRepository.save(budget);
    }

    private void updateBudgetSpending(Budget budget) {
        List<Transaction> transactions = transactionRepository.findByTransactionDateBetweenAndCategory(
            budget.getStartDate(),
            budget.getEndDate(),
            budget.getCategory()
        );

        BigDecimal totalSpent = transactions.stream()
            .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        budget.setSpentAmount(totalSpent);
    }

    private void validateBudget(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }
        
        if (budget.getName() == null || budget.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Budget name cannot be empty");
        }

        if (budget.getBudgetLimit() == null || budget.getBudgetLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget limit must be greater than zero");
        }

        if (budget.getStartDate() == null || budget.getEndDate() == null) {
            throw new IllegalArgumentException("Budget start and end dates are required");
        }

        if (budget.getStartDate().isAfter(budget.getEndDate())) {
            throw new IllegalArgumentException("Budget start date cannot be after end date");
        }

        if (budget.getCategory() == null || budget.getCategory().getId() == null) {
            throw new IllegalArgumentException("Budget must be associated with a category");
        }

        // Verify category exists
        categoryRepository.findById(budget.getCategory().getId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + budget.getCategory().getId()));

        // Check for overlapping budgets for the same category
        List<Budget> overlappingBudgets = budgetRepository.findOverlappingBudgets(
            budget.getCategory().getId(),
            budget.getStartDate(),
            budget.getEndDate(),
            budget.getId() != null ? budget.getId() : -1L
        );

        if (!overlappingBudgets.isEmpty()) {
            throw new IllegalArgumentException("Another budget already exists for this category during the specified period");
        }
    }

    public BigDecimal calculateRemainingBudget(Long budgetId) {
        Budget budget = getBudgetById(budgetId);
        updateBudgetSpending(budget);
        return budget.getBudgetLimit().subtract(budget.getSpentAmount());
    }

    public List<Budget> getBudgetsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        return budgetRepository.findByCategoryId(categoryId);
    }
}
