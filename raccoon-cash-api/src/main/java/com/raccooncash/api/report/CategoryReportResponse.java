package com.raccooncash.api.report;

import java.math.BigDecimal;

public class CategoryReportResponse {
    private Long categoryId;
    private String categoryName;
    private BigDecimal totalExpense = BigDecimal.ZERO;

    public CategoryReportResponse() {
    }

    public CategoryReportResponse(Long categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public void addExpense(BigDecimal amount) {
        totalExpense = totalExpense.add(amount);
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

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }
}