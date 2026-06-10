package com.raccooncash.api.debt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DebtRequest {

    @NotBlank(message = "El nombre de la persona es obligatorio")
    private String personName;

    private String description;

    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto total debe ser mayor a cero")
    private BigDecimal totalAmount;

    @NotNull(message = "El tipo de deuda es obligatorio")
    private DebtType type;

    private LocalDate dueDate;

    @NotNull(message = "La cuenta es obligatoria")
    private Long accountId;

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public DebtType getType() {
        return type;
    }

    public void setType(DebtType type) {
        this.type = type;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}