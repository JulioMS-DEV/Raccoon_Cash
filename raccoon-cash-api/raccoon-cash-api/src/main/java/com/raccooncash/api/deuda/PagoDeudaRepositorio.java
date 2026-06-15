package com.raccooncash.api.deuda;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoDeudaRepositorio extends JpaRepository<PagoDeuda, Long> {
    List<PagoDeuda> findAllByDebtIdOrderByPaymentDateDesc(Long debtId);
}