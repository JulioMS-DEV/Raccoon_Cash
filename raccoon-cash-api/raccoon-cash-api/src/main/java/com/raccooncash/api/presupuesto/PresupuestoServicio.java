package com.raccooncash.api.presupuesto;

import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
import com.raccooncash.api.transaccion.Transaccion;
import com.raccooncash.api.transaccion.TransaccionRepositorio;
import com.raccooncash.api.transaccion.TipoTransaccion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresupuestoServicio {

    private final PresupuestoRepositorio budgetRepository;
    private final TransaccionRepositorio transactionRepository;

    public PresupuestoServicio(PresupuestoRepositorio budgetRepository, TransaccionRepositorio transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<PresupuestoRespuesta> getAllBudgets() {
        return budgetRepository.findAllByActiveTrueOrderByStartDateDesc()
                .stream()
                .map(PresupuestoRespuesta::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PresupuestoRespuesta getBudgetById(Long id) {
        return new PresupuestoRespuesta(findActiveBudget(id));
    }

    @Transactional
    public PresupuestoRespuesta createBudget(PresupuestoSolicitud request) {
        validateRequest(request);

        Presupuesto budget = new Presupuesto();
        fillBudget(budget, request);
        budget.setActive(true);

        Presupuesto savedBudget = budgetRepository.save(budget);
        return new PresupuestoRespuesta(savedBudget);
    }

    @Transactional
    public PresupuestoRespuesta updateBudget(Long id, PresupuestoSolicitud request) {
        validateRequest(request);

        Presupuesto budget = findActiveBudget(id);
        fillBudget(budget, request);

        Presupuesto updatedBudget = budgetRepository.save(budget);
        return new PresupuestoRespuesta(updatedBudget);
    }

    @Transactional
    public void deleteBudget(Long id) {
        Presupuesto budget = findActiveBudget(id);
        budget.setActive(false);
        budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public ResumenPresupuestoRespuesta getBudgetSummary(Long id) {
        Presupuesto budget = findActiveBudget(id);

        List<Transaccion> expenses = transactionRepository.findWithFilters(
                null,
                null,
                TipoTransaccion.EXPENSE,
                budget.getStartDate().atStartOfDay(),
                budget.getEndDate().atTime(LocalTime.MAX));

        BigDecimal spent = expenses.stream()
                .map(Transaccion::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = budget.getAmountLimit().subtract(spent);
        BigDecimal percentageUsed = spent
                .multiply(BigDecimal.valueOf(100))
                .divide(budget.getAmountLimit(), 2, RoundingMode.HALF_UP);

        return new ResumenPresupuestoRespuesta(
                budget.getName(),
                budget.getAmountLimit(),
                spent,
                remaining,
                percentageUsed);
    }

    private void fillBudget(Presupuesto budget, PresupuestoSolicitud request) {
        budget.setName(request.getName());
        budget.setAmountLimit(request.getAmountLimit());
        budget.setPeriodType(request.getPeriodType());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
    }

    private void validateRequest(PresupuestoSolicitud request) {
        if (request.getAmountLimit() == null || request.getAmountLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudIncorrectaException("El limite debe ser mayor a cero");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new SolicitudIncorrectaException("Las fechas del presupuesto son obligatorias");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new SolicitudIncorrectaException("La fecha final no puede ser anterior a la fecha inicial");
        }
    }

    Presupuesto findActiveBudget(Long id) {
        return budgetRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));
    }
}