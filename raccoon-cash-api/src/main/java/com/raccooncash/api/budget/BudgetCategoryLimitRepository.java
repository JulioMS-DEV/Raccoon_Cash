package com.raccooncash.api.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetCategoryLimitRepository extends JpaRepository<BudgetCategoryLimit, Long> {
    List<BudgetCategoryLimit> findAllByBudgetId(Long budgetId);
    Optional<BudgetCategoryLimit> findByIdAndBudgetId(Long id, Long budgetId);
}