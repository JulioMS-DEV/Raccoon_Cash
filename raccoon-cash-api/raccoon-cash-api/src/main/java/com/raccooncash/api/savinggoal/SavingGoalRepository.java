package com.raccooncash.api.savinggoal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {
    List<SavingGoal> findAllByUsuarioId(Long usuarioId);
    Optional<SavingGoal> findByIdAndUsuarioId(Long id, Long usuarioId);
}
