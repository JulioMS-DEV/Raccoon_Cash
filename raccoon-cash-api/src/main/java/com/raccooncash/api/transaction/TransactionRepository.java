package com.raccooncash.api.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByActiveTrueOrderByDateDesc();

    @Query("""
            SELECT t FROM FinancialTransaction t
            JOIN FETCH t.account account
            LEFT JOIN FETCH t.toAccount destination
            LEFT JOIN FETCH t.category category
            WHERE t.id = :id
              AND (t.active = true OR t.active IS NULL)
            """)
    Optional<Transaction> findActiveById(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT t FROM FinancialTransaction t
            JOIN FETCH t.account account
            LEFT JOIN FETCH t.toAccount destination
            LEFT JOIN FETCH t.category category
            WHERE (t.active = true OR t.active IS NULL)
              AND (:accountId IS NULL OR account.id = :accountId OR destination.id = :accountId)
              AND (:categoryId IS NULL OR category.id = :categoryId)
              AND (:type IS NULL OR t.type = :type)
              AND (:fromDate IS NULL OR t.date >= :fromDate)
              AND (:toDate IS NULL OR t.date <= :toDate)
            ORDER BY t.date DESC
            """)
    List<Transaction> findWithFilters(@Param("accountId") Long accountId,
                                      @Param("categoryId") Long categoryId,
                                      @Param("type") TransactionType type,
                                      @Param("fromDate") LocalDateTime fromDate,
                                      @Param("toDate") LocalDateTime toDate);
}
