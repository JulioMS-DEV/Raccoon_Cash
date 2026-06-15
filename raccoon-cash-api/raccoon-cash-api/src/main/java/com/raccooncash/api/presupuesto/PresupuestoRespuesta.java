package com.raccooncash.api.presupuesto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PresupuestoRespuesta {
    private Long id;
    private String name;
    private BigDecimal amountLimit;
    private TipoPeriodoPresupuesto periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PresupuestoRespuesta() {
    }

    public PresupuestoRespuesta(Presupuesto budget) {
        this.id = budget.getId();
        this.name = budget.getName();
        this.amountLimit = budget.getAmountLimit();
        this.periodType = budget.getPeriodType();
        this.startDate = budget.getStartDate();
        this.endDate = budget.getEndDate();
        this.active = budget.getActive();
        this.createdAt = budget.getCreatedAt();
        this.updatedAt = budget.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public TipoPeriodoPresupuesto getPeriodType() {
        return periodType;
    }

    public void setPeriodType(TipoPeriodoPresupuesto periodType) {
        this.periodType = periodType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}