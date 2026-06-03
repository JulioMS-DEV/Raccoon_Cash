package com.raccooncash.api.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findAllByActiveTrueOrderByStartDateDesc();
    Optional<Budget> findByIdAndActiveTrue(Long id);
    long countByActiveTrue();
}