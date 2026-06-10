package com.raccooncash.api.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, TransactionRepositoryCustom {
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
}
