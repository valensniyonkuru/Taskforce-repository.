package com.taskforce.wallet_app.repository;

import com.taskforce.wallet_app.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    @Query("SELECT b FROM Budget b WHERE b.spentAmount > b.budgetLimit")
    List<Budget> findBudgetsExceedingLimit();
    
    List<Budget> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date, LocalDate sameDate);
    
    List<Budget> findByCategoryId(Long categoryId);

    @Query("SELECT b FROM Budget b WHERE " +
           "b.id != :budgetId AND b.category.id = :categoryId AND " +
           "((b.startDate BETWEEN :startDate AND :endDate) OR " +
           "(b.endDate BETWEEN :startDate AND :endDate) OR " +
           "(b.startDate <= :startDate AND b.endDate >= :endDate))")
    List<Budget> findOverlappingBudgets(
            @Param("budgetId") Long budgetId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId);

    @Query("SELECT b FROM Budget b WHERE " +
           "b.id = :budgetId AND " +
           "b.startDate <= :endDate AND " +
           "b.endDate >= :startDate AND " +
           "b.category.id = :categoryId")
    List<Budget> findBudgetsByDateRangeAndCategory(
            @Param("budgetId") Long budgetId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId);
}
