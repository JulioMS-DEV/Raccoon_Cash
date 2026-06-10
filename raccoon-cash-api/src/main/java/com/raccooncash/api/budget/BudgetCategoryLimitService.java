package com.raccooncash.api.budget;

import com.raccooncash.api.category.Category;
import com.raccooncash.api.category.CategoryRepository;
import com.raccooncash.api.category.CategoryType;
import com.raccooncash.api.exception.BadRequestException;
import com.raccooncash.api.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetCategoryLimitService {

    private final BudgetService budgetService;
    private final BudgetCategoryLimitRepository limitRepository;
    private final CategoryRepository categoryRepository;

    public BudgetCategoryLimitService(BudgetService budgetService,
                                      BudgetCategoryLimitRepository limitRepository,
                                      CategoryRepository categoryRepository) {
        this.budgetService = budgetService;
        this.limitRepository = limitRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<BudgetCategoryLimitResponse> getLimits(Long budgetId) {
        budgetService.findActiveBudget(budgetId);
        return limitRepository.findAllByBudgetId(budgetId)
                .stream()
                .map(BudgetCategoryLimitResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetCategoryLimitResponse createLimit(Long budgetId, BudgetCategoryLimitRequest request) {
        validateAmount(request.getAmountLimit());
        Budget budget = budgetService.findActiveBudget(budgetId);
        Category category = findExpenseCategory(request.getCategoryId());

        BudgetCategoryLimit limit = new BudgetCategoryLimit();
        limit.setBudget(budget);
        limit.setCategory(category);
        limit.setAmountLimit(request.getAmountLimit());

        BudgetCategoryLimit savedLimit = limitRepository.save(limit);
        return new BudgetCategoryLimitResponse(savedLimit);
    }

    @Transactional
    public BudgetCategoryLimitResponse updateLimit(Long budgetId, Long limitId, BudgetCategoryLimitRequest request) {
        validateAmount(request.getAmountLimit());
        budgetService.findActiveBudget(budgetId);
        BudgetCategoryLimit limit = findLimit(budgetId, limitId);
        Category category = findExpenseCategory(request.getCategoryId());

        limit.setCategory(category);
        limit.setAmountLimit(request.getAmountLimit());

        BudgetCategoryLimit updatedLimit = limitRepository.save(limit);
        return new BudgetCategoryLimitResponse(updatedLimit);
    }

    @Transactional
    public void deleteLimit(Long budgetId, Long limitId) {
        budgetService.findActiveBudget(budgetId);
        BudgetCategoryLimit limit = findLimit(budgetId, limitId);
        limitRepository.delete(limit);
    }

    private BudgetCategoryLimit findLimit(Long budgetId, Long limitId) {
        return limitRepository.findByIdAndBudgetId(limitId, budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Limite de categoria no encontrado"));
    }

    private Category findExpenseCategory(Long categoryId) {
        Category category = categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new BadRequestException("Solo se pueden presupuestar categorias de gasto");
        }

        return category;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El limite debe ser mayor a cero");
        }
    }
}