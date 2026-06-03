package com.raccooncash.api.budget;

import java.math.BigDecimal;

public class BudgetCategoryLimitResponse {
    private Long id;
    private Long budgetId;
    private String budgetName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amountLimit;

    public BudgetCategoryLimitResponse() {
    }

    public BudgetCategoryLimitResponse(BudgetCategoryLimit limit) {
        this.id = limit.getId();
        this.budgetId = limit.getBudget().getId();
        this.budgetName = limit.getBudget().getName();
        this.categoryId = limit.getCategory().getId();
        this.categoryName = limit.getCategory().getName();
        this.amountLimit = limit.getAmountLimit();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public String getBudgetName() {
        return budgetName;
    }

    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }
}