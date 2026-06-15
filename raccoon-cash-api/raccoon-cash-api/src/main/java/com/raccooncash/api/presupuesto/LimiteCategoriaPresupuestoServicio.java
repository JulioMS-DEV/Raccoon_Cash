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
    public List<LimiteCategoriaPresupuestoRespuesta> getLimits(Long budgetId) {
        budgetService.findActiveBudget(budgetId);
        return limitRepository.findAllByBudgetId(budgetId)
                .stream()
                .map(LimiteCategoriaPresupuestoRespuesta::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public LimiteCategoriaPresupuestoRespuesta createLimit(Long budgetId, LimiteCategoriaPresupuestoSolicitud request) {
        validateAmount(request.getAmountLimit());
        Presupuesto budget = budgetService.findActiveBudget(budgetId);
        Categoria category = findExpenseCategory(request.getCategoryId());

        LimiteCategoriaPresupuesto limit = new LimiteCategoriaPresupuesto();
        limit.setBudget(budget);
        limit.setCategory(category);
        limit.setAmountLimit(request.getAmountLimit());

        LimiteCategoriaPresupuesto savedLimit = limitRepository.save(limit);
        return new LimiteCategoriaPresupuestoRespuesta(savedLimit);
    }

    @Transactional
    public LimiteCategoriaPresupuestoRespuesta updateLimit(Long budgetId, Long limitId, LimiteCategoriaPresupuestoSolicitud request) {
        validateAmount(request.getAmountLimit());
        budgetService.findActiveBudget(budgetId);
        LimiteCategoriaPresupuesto limit = findLimit(budgetId, limitId);
        Categoria category = findExpenseCategory(request.getCategoryId());

        limit.setCategory(category);
        limit.setAmountLimit(request.getAmountLimit());

        LimiteCategoriaPresupuesto updatedLimit = limitRepository.save(limit);
        return new LimiteCategoriaPresupuestoRespuesta(updatedLimit);
    }

    @Transactional
    public void deleteLimit(Long budgetId, Long limitId) {
        budgetService.findActiveBudget(budgetId);
        LimiteCategoriaPresupuesto limit = findLimit(budgetId, limitId);
        limitRepository.delete(limit);
    }

    private LimiteCategoriaPresupuesto findLimit(Long budgetId, Long limitId) {
        return limitRepository.findByIdAndBudgetId(limitId, budgetId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Limite de categoria no encontrado"));
    }

    private Categoria findExpenseCategory(Long categoryId) {
        Categoria category = categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada"));

        if (category.getType() != TipoCategoria.EXPENSE) {
            throw new SolicitudIncorrectaException("Solo se pueden presupuestar categorias de gasto");
        }

        return category;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudIncorrectaException("El limite debe ser mayor a cero");
        }
    }
}