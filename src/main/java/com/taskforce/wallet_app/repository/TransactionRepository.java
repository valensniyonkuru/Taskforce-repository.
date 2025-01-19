package com.taskforce.wallet_app.repository;

import com.taskforce.wallet_app.model.Transaction;
import com.taskforce.wallet_app.model.Category;
import com.taskforce.wallet_app.model.Transaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions by type (income or expense)
    List<Transaction> findByType(TransactionType type);

    // Find all transactions within a specific date range
    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);

    // Find all transactions by category
    List<Transaction> findByCategory(Category category);

    // Find all transactions by category ordered by transaction date in descending order
    List<Transaction> findByCategoryOrderByTransactionDateDesc(Category category);

    // Find all transactions within a specific date range ordered by transaction date in descending order
    List<Transaction> findByTransactionDateBetweenOrderByTransactionDateDesc(LocalDate startDate, LocalDate endDate);

    // Find all transactions by type ordered by transaction date in descending order
    List<Transaction> findByTypeOrderByTransactionDateDesc(TransactionType type);

    // Find transactions by category and date range
    List<Transaction> findByCategoryAndTransactionDateBetween(Category category, LocalDate startDate, LocalDate endDate);

    // Find transactions by date range and category
    List<Transaction> findByTransactionDateBetweenAndCategory(LocalDate startDate, LocalDate endDate, Category category);

    // Find transactions by category, date range and type
    List<Transaction> findByCategoryAndTransactionDateBetweenAndType(Category category, LocalDate startDate, LocalDate endDate, TransactionType type);

    // Find transactions by category ID, date range and type
    @Query("SELECT t FROM Transaction t WHERE t.category.id = :categoryId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.type = :type")
    List<Transaction> findByCategoryIdAndTransactionDateBetweenAndType(
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") TransactionType type);
}
