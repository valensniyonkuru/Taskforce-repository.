package com.taskforce.wallet_app.repository;

import com.taskforce.wallet_app.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions by type (income or expense)
    List<Transaction> findByType(String type);

    // Find all transactions within a specific date range
    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);

    // Find all transactions by account type
    List<Transaction> findByAccount(String account);
}
