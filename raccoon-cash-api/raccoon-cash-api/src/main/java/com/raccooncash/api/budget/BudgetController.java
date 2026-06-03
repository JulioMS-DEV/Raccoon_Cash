package com.raccooncash.api.budget;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetCategoryLimitService limitService;

    public BudgetController(BudgetService budgetService, BudgetCategoryLimitService limitService) {
        this.budgetService = budgetService;
        this.limitService = limitService;
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.createBudget(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(@PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<BudgetSummaryResponse> getBudgetSummary(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetSummary(id));
    }

    @GetMapping("/{budgetId}/categories")
    public ResponseEntity<List<BudgetCategoryLimitResponse>> getCategoryLimits(@PathVariable Long budgetId) {
        return ResponseEntity.ok(limitService.getLimits(budgetId));
    }

    @PostMapping("/{budgetId}/categories")
    public ResponseEntity<BudgetCategoryLimitResponse> createCategoryLimit(@PathVariable Long budgetId,
                                                                            @Valid @RequestBody BudgetCategoryLimitRequest request) {
        return ResponseEntity.ok(limitService.createLimit(budgetId, request));
    }

    @PutMapping("/{budgetId}/categories/{limitId}")
    public ResponseEntity<BudgetCategoryLimitResponse> updateCategoryLimit(@PathVariable Long budgetId,
                                                                            @PathVariable Long limitId,
                                                                            @Valid @RequestBody BudgetCategoryLimitRequest request) {
        return ResponseEntity.ok(limitService.updateLimit(budgetId, limitId, request));
    }

    @DeleteMapping("/{budgetId}/categories/{limitId}")
    public ResponseEntity<Void> deleteCategoryLimit(@PathVariable Long budgetId, @PathVariable Long limitId) {
        limitService.deleteLimit(budgetId, limitId);
        return ResponseEntity.noContent().build();
    }
}