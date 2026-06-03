package com.raccooncash.api.debt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findAllByActiveTrueOrderByCreatedAtDesc();
    List<Debt> findAllByActiveTrue();
    Optional<Debt> findByIdAndActiveTrue(Long id);
}