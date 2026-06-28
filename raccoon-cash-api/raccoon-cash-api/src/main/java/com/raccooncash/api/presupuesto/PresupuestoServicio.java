package com.raccooncash.api.presupuesto;

import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
import com.raccooncash.api.transaccion.Transaccion;
import com.raccooncash.api.transaccion.TransaccionRepositorio;
import com.raccooncash.api.usuario.Usuario;
import com.raccooncash.api.usuario.UsuarioServicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresupuestoServicio {

    private final PresupuestoRepositorio budgetRepository;
    private final TransaccionRepositorio transactionRepository;
    private final UsuarioServicio usuarioServicio;

    public PresupuestoServicio(PresupuestoRepositorio budgetRepository,
                               TransaccionRepositorio transactionRepository,
                               UsuarioServicio usuarioServicio) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.usuarioServicio = usuarioServicio;
    }

    @Transactional(readOnly = true)
    public List<PresupuestoRespuesta> getAllBudgets(Long usuarioId) {
        return budgetRepository.findAllByUsuarioIdAndActiveTrueOrderByStartDateDesc(usuarioId)
                .stream()
                .map(budget -> toResponse(budget, usuarioId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PresupuestoRespuesta getBudgetById(Long usuarioId, Long id) {
        return toResponse(findActiveBudget(usuarioId, id), usuarioId);
    }

    @Transactional
    public PresupuestoRespuesta createBudget(Long usuarioId, PresupuestoSolicitud request) {
        validateRequest(request);
        Usuario usuario = usuarioServicio.obtenerUsuario(usuarioId);

        Presupuesto budget = new Presupuesto();
        budget.setUsuario(usuario);
        fillBudget(budget, request);
        budget.setActive(true);

        Presupuesto savedBudget = budgetRepository.save(budget);
        return toResponse(savedBudget, usuarioId);
    }

    @Transactional
    public PresupuestoRespuesta updateBudget(Long usuarioId, Long id, PresupuestoSolicitud request) {
        validateRequest(request);

        Presupuesto budget = findActiveBudget(usuarioId, id);
        fillBudget(budget, request);

        Presupuesto updatedBudget = budgetRepository.save(budget);
        return toResponse(updatedBudget, usuarioId);
    }

    @Transactional
    public void deleteBudget(Long usuarioId, Long id) {
        Presupuesto budget = findActiveBudget(usuarioId, id);
        budget.setActive(false);
        budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public ResumenPresupuestoRespuesta getBudgetSummary(Long usuarioId, Long id) {
        Presupuesto budget = findActiveBudget(usuarioId, id);
        BigDecimal spent = calculateCurrentAmount(budget, usuarioId);

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
        budget.setName(request.getNombre());
        budget.setAmountLimit(BigDecimal.valueOf(request.getMonto()));
        budget.setPeriodType(request.getTipoPeriodo());
        budget.setPeriodValue(request.getValorPeriodo());
        budget.setStartDate(request.getFechaInicio());
        budget.setEndDate(calculateEndDate(request.getFechaInicio(), request.getTipoPeriodo(), request.getValorPeriodo()));
        budget.setColor(request.getColor());
        budget.setExpense(request.getEsGasto());
        budget.setIncludeAllTransactions(request.getIncluirTodasLasTransacciones());
    }

    private void validateRequest(PresupuestoSolicitud request) {
        if (request.getMonto() == null || request.getMonto() <= 0) {
            throw new SolicitudIncorrectaException("El monto debe ser mayor a cero");
        }
        if (request.getTipoPeriodo() == null) {
            throw new SolicitudIncorrectaException("El periodo es obligatorio");
        }
        if (request.getValorPeriodo() == null || request.getValorPeriodo() <= 0) {
            throw new SolicitudIncorrectaException("El valor del periodo debe ser mayor a cero");
        }
        if (request.getFechaInicio() == null) {
            throw new SolicitudIncorrectaException("La fecha inicial es obligatoria");
        }
        if (request.getEsGasto() == null) {
            throw new SolicitudIncorrectaException("El tipo de presupuesto es obligatorio");
        }
        if (request.getIncluirTodasLasTransacciones() == null) {
            throw new SolicitudIncorrectaException("Debe indicar si se incluyen todas las transacciones");
        }
    }

    private LocalDate calculateEndDate(LocalDate startDate, TipoPeriodoPresupuesto periodType, Integer periodValue) {
        return switch (periodType) {
            case DIARIO -> startDate.plusDays(periodValue).minusDays(1);
            case SEMANAL -> startDate.plusWeeks(periodValue).minusDays(1);
            case MENSUAL -> startDate.plusMonths(periodValue).minusDays(1);
            case ANUAL -> startDate.plusYears(periodValue).minusDays(1);
            case PERSONALIZADO -> startDate.plusDays(periodValue).minusDays(1);
        };
    }

    private PresupuestoRespuesta toResponse(Presupuesto budget, Long usuarioId) {
        return new PresupuestoRespuesta(budget, calculateCurrentAmount(budget, usuarioId));
    }

    private BigDecimal calculateCurrentAmount(Presupuesto budget, Long usuarioId) {
        return transactionRepository.findByBudgetAndUsuarioIdAndActive(budget, usuarioId).stream()
                .map(Transaccion::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    Presupuesto findActiveBudget(Long usuarioId, Long id) {
        return budgetRepository.findByIdAndUsuarioIdAndActiveTrue(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));
    }
}
