package com.raccooncash.api.transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Transaction> findWithFilters(Long accountId,
                                             Long categoryId,
                                             TransactionType type,
                                             LocalDateTime fromDate,
                                             LocalDateTime toDate) {
        StringBuilder jpql = new StringBuilder("""
                SELECT DISTINCT t FROM FinancialTransaction t
                JOIN FETCH t.account account
                LEFT JOIN FETCH t.toAccount destination
                LEFT JOIN FETCH t.category category
                WHERE (t.active = true OR t.active IS NULL)
                """);

        if (accountId != null) {
            jpql.append(" AND (account.id = :accountId OR destination.id = :accountId)");
        }
        if (categoryId != null) {
            jpql.append(" AND category.id = :categoryId");
        }
        if (type != null) {
            jpql.append(" AND t.type = :type");
        }
        if (fromDate != null) {
            jpql.append(" AND t.date >= :fromDate");
        }
        if (toDate != null) {
            jpql.append(" AND t.date <= :toDate");
        }

        jpql.append(" ORDER BY t.date DESC");

        TypedQuery<Transaction> query = entityManager.createQuery(jpql.toString(), Transaction.class);
        if (accountId != null) {
            query.setParameter("accountId", accountId);
        }
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        if (type != null) {
            query.setParameter("type", type);
        }
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }

        return query.getResultList();
    }
}
