package com.raccooncash.api.budget;

import com.raccooncash.api.exception.BadRequestException;
import com.raccooncash.api.exception.ResourceNotFoundException;
import com.raccooncash.api.transaction.Transaction;
import com.raccooncash.api.transaction.TransactionRepository;
import com.raccooncash.api.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository, TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets() {
        return budgetRepository.findAllByActiveTrueOrderByStartDateDesc()
                .stream()
                .map(BudgetResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {
        return new BudgetResponse(findActiveBudget(id));
    }

    @Transactional
    public BudgetResponse createBudget(BudgetRequest request) {
        validateRequest(request);

        Budget budget = new Budget();
        fillBudget(budget, request);
        budget.setActive(true);

        Budget savedBudget = budgetRepository.save(budget);
        return new BudgetResponse(savedBudget);
    }

    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        validateRequest(request);

        Budget budget = findActiveBudget(id);
        fillBudget(budget, request);

        Budget updatedBudget = budgetRepository.save(budget);
        return new BudgetResponse(updatedBudget);
    }

    @Transactional
    public void deleteBudget(Long id) {
        Budget budget = findActiveBudget(id);
        budget.setActive(false);
        budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public BudgetSummaryResponse getBudgetSummary(Long id) {
        Budget budget = findActiveBudget(id);

        List<Transaction> expenses = transactionRepository.findWithFilters(
                null,
                null,
                TransactionType.EXPENSE,
                budget.getStartDate().atStartOfDay(),
                budget.getEndDate().atTime(LocalTime.MAX));

        BigDecimal spent = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = budget.getAmountLimit().subtract(spent);
        BigDecimal percentageUsed = spent
                .multiply(BigDecimal.valueOf(100))
                .divide(budget.getAmountLimit(), 2, RoundingMode.HALF_UP);

        return new BudgetSummaryResponse(
                budget.getName(),
                budget.getAmountLimit(),
                spent,
                remaining,
                percentageUsed);
    }

    private void fillBudget(Budget budget, BudgetRequest request) {
        budget.setName(request.getName());
        budget.setAmountLimit(request.getAmountLimit());
        budget.setPeriodType(request.getPeriodType());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
    }

    private void validateRequest(BudgetRequest request) {
        if (request.getAmountLimit() == null || request.getAmountLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El limite debe ser mayor a cero");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BadRequestException("Las fechas del presupuesto son obligatorias");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("La fecha final no puede ser anterior a la fecha inicial");
        }
    }

    Budget findActiveBudget(Long id) {
        return budgetRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado"));
    }
}