package com.raccooncash.api.deuda;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoDeudaRepositorio extends JpaRepository<PagoDeuda, Long> {
    List<PagoDeuda> findAllByDebt_IdAndDebt_Usuario_IdAndActiveTrueOrderByPaymentDateDescCreatedAtDesc(Long debtId, Long usuarioId);
    List<PagoDeuda> findAllByDebt_IdAndDebt_Usuario_IdAndActiveTrue(Long debtId, Long usuarioId);
    Optional<PagoDeuda> findByIdAndDebt_IdAndDebt_Usuario_IdAndActiveTrue(Long id, Long debtId, Long usuarioId);
    boolean existsByDebt_IdAndDebt_Usuario_IdAndActiveTrue(Long debtId, Long usuarioId);
}
