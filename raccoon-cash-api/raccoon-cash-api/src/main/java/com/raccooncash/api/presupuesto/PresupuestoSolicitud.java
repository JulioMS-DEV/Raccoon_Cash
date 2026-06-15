package com.raccooncash.api.presupuesto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PresupuestoSolicitud {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "El limite del presupuesto es obligatorio")
    @Positive(message = "El limite debe ser mayor a cero")
    private BigDecimal amountLimit;

    @NotNull(message = "El periodo es obligatorio")
    private TipoPeriodoPresupuesto periodType;

    @NotNull(message = "La fecha inicial es obligatoria")
    private LocalDate startDate;

    @NotNull(message = "La fecha final es obligatoria")
    private LocalDate endDate;

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
}