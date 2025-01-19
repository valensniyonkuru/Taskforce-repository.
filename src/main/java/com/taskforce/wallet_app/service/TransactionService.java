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
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetService budgetService;

    public TransactionService(TransactionRepository transactionRepository,
                            CategoryRepository categoryRepository,
                            BudgetRepository budgetRepository,
                            BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
        this.budgetService = budgetService;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        validateTransaction(transaction);
        
        // Set default values if needed
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Update associated budget if exists
        updateAssociatedBudget(savedTransaction);
        
        return savedTransaction;
    }

    @Transactional
    public Transaction updateTransaction(Long id, Transaction transactionDetails) {
        Transaction transaction = getTransactionById(id);
        validateTransaction(transactionDetails);

        // Store old category for budget updates
        Category oldCategory = transaction.getCategory();
        BigDecimal oldAmount = transaction.getAmount();
        
        // Update transaction details
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setDescription(transactionDetails.getDescription());
        transaction.setType(transactionDetails.getType());
        transaction.setTransactionDate(transactionDetails.getTransactionDate());
        transaction.setCategory(transactionDetails.getCategory());
        transaction.setPaymentMethod(transactionDetails.getPaymentMethod());
        transaction.setNotes(transactionDetails.getNotes());
        transaction.setRecurring(transactionDetails.isRecurring());
        transaction.setReferenceNumber(transactionDetails.getReferenceNumber());

        Transaction updatedTransaction = transactionRepository.save(transaction);
        
        // Update budgets if category changed or amount changed
        if ((oldCategory != null && !oldCategory.equals(transaction.getCategory())) ||
            (oldAmount != null && !oldAmount.equals(transaction.getAmount()))) {
            updateBudgetsForCategoryChange(oldCategory, transaction.getCategory(), transaction.getTransactionDate());
        }
        
        return updatedTransaction;
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = getTransactionById(id);
        transactionRepository.delete(transaction);
        
        // Update associated budget
        if (transaction.getCategory() != null) {
            updateBudgetsForCategory(transaction.getCategory(), transaction.getTransactionDate());
        }
    }

    public List<Transaction> getTransactionsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        return transactionRepository.findByCategory(category);
    }

    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }

    public List<Transaction> getTransactionsByCategoryAndDateRange(Long categoryId, LocalDate startDate, LocalDate endDate) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        return transactionRepository.findByCategoryAndTransactionDateBetween(category, startDate, endDate);
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        if (transaction.getType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        if (transaction.getCategory() != null && transaction.getCategory().getId() != null) {
            if (!categoryRepository.existsById(transaction.getCategory().getId())) {
                throw new EntityNotFoundException("Category not found with id: " + transaction.getCategory().getId());
            }
        }

        if (transaction.getTransactionDate() != null && transaction.getTransactionDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Transaction date cannot be in the future");
        }
    }

    private void updateAssociatedBudget(Transaction transaction) {
        if (transaction.getCategory() != null) {
            updateBudgetsForCategory(transaction.getCategory(), transaction.getTransactionDate());
        }
    }

    private void updateBudgetsForCategory(Category category, LocalDate transactionDate) {
        List<Budget> budgets = budgetRepository.findByCategoryId(category.getId());
        for (Budget budget : budgets) {
            if (isTransactionInBudgetPeriod(transactionDate, budget)) {
                budgetService.updateBudgetSpending(budget.getId());
            }
        }
    }

    private void updateBudgetsForCategoryChange(Category oldCategory, Category newCategory, LocalDate transactionDate) {
        if (oldCategory != null) {
            updateBudgetsForCategory(oldCategory, transactionDate);
        }
        if (newCategory != null) {
            updateBudgetsForCategory(newCategory, transactionDate);
        }
    }

    private boolean isTransactionInBudgetPeriod(LocalDate transactionDate, Budget budget) {
        return !transactionDate.isBefore(budget.getStartDate()) && 
               !transactionDate.isAfter(budget.getEndDate());
    }

    public BigDecimal getTotalExpensesByCategory(Long categoryId, LocalDate startDate, LocalDate endDate) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        
        List<Transaction> transactions = transactionRepository.findByCategoryIdAndTransactionDateBetweenAndType(
            categoryId, startDate, endDate, Transaction.TransactionType.EXPENSE);
        
        return transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalIncomeByCategory(Long categoryId, LocalDate startDate, LocalDate endDate) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        
        List<Transaction> transactions = transactionRepository.findByCategoryIdAndTransactionDateBetweenAndType(
            categoryId, startDate, endDate, Transaction.TransactionType.INCOME);
        
        return transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
