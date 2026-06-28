package com.raccooncash.api.presupuesto;

import com.raccooncash.api.categoria.Categoria;
import com.raccooncash.api.categoria.CategoriaRepositorio;
import com.raccooncash.api.categoria.TipoCategoria;
import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LimiteCategoriaPresupuestoServicio {

    private final PresupuestoServicio budgetService;
    private final LimiteCategoriaPresupuestoRepositorio limitRepository;
    private final CategoriaRepositorio categoryRepository;

    public LimiteCategoriaPresupuestoServicio(PresupuestoServicio budgetService,
                                      LimiteCategoriaPresupuestoRepositorio limitRepository,
                                      CategoriaRepositorio categoryRepository) {
        this.budgetService = budgetService;
        this.limitRepository = limitRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<LimiteCategoriaPresupuestoRespuesta> getLimits(Long usuarioId, Long budgetId) {
        budgetService.findActiveBudget(usuarioId, budgetId);
        return limitRepository.findAllByBudget_IdAndBudget_Usuario_Id(budgetId, usuarioId)
                .stream()
                .map(LimiteCategoriaPresupuestoRespuesta::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public LimiteCategoriaPresupuestoRespuesta createLimit(Long usuarioId, Long budgetId, LimiteCategoriaPresupuestoSolicitud request) {
        validateAmount(request.getAmountLimit());
        Presupuesto budget = budgetService.findActiveBudget(usuarioId, budgetId);
        Categoria category = findBudgetCategory(budget, request.getCategoryId());

        LimiteCategoriaPresupuesto limit = new LimiteCategoriaPresupuesto();
        limit.setBudget(budget);
        limit.setCategory(category);
        limit.setAmountLimit(request.getAmountLimit());

        LimiteCategoriaPresupuesto savedLimit = limitRepository.save(limit);
        return new LimiteCategoriaPresupuestoRespuesta(savedLimit);
    }

    @Transactional
    public LimiteCategoriaPresupuestoRespuesta updateLimit(Long usuarioId, Long budgetId, Long limitId, LimiteCategoriaPresupuestoSolicitud request) {
        validateAmount(request.getAmountLimit());
        Presupuesto budget = budgetService.findActiveBudget(usuarioId, budgetId);
        LimiteCategoriaPresupuesto limit = findLimit(usuarioId, budgetId, limitId);
        Categoria category = findBudgetCategory(budget, request.getCategoryId());

        limit.setCategory(category);
        limit.setAmountLimit(request.getAmountLimit());

        LimiteCategoriaPresupuesto updatedLimit = limitRepository.save(limit);
        return new LimiteCategoriaPresupuestoRespuesta(updatedLimit);
    }

    @Transactional
    public void deleteLimit(Long usuarioId, Long budgetId, Long limitId) {
        budgetService.findActiveBudget(usuarioId, budgetId);
        LimiteCategoriaPresupuesto limit = findLimit(usuarioId, budgetId, limitId);
        limitRepository.delete(limit);
    }

    private LimiteCategoriaPresupuesto findLimit(Long usuarioId, Long budgetId, Long limitId) {
        return limitRepository.findByIdAndBudget_IdAndBudget_Usuario_Id(limitId, budgetId, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Limite de categoria no encontrado"));
    }

    private Categoria findBudgetCategory(Presupuesto budget, Long categoryId) {
        Categoria category = categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada"));

        TipoCategoria expectedType = Boolean.TRUE.equals(budget.getExpense()) ? TipoCategoria.EXPENSE : TipoCategoria.INCOME;
        if (category.getType() != expectedType) {
            String expectedLabel = expectedType == TipoCategoria.EXPENSE ? "gasto" : "ingreso";
            throw new SolicitudIncorrectaException("La categoria debe ser de tipo " + expectedLabel);
        }

        return category;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudIncorrectaException("El limite debe ser mayor a cero");
        }
    }
}
