package com.raccooncash.api.presupuesto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LimiteCategoriaPresupuestoRepositorio extends JpaRepository<LimiteCategoriaPresupuesto, Long> {
    List<LimiteCategoriaPresupuesto> findAllByBudget_IdAndBudget_Usuario_Id(Long budgetId, Long usuarioId);
    Optional<LimiteCategoriaPresupuesto> findByIdAndBudget_IdAndBudget_Usuario_Id(Long id, Long budgetId, Long usuarioId);
}
