package com.raccooncash.api.debt;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DebtPaymentRequest {

    @NotNull(message = "El monto del pago es obligatorio")
    @Positive(message = "El pago debe ser mayor a cero")
    private BigDecimal amount;

    private LocalDate paymentDate;
    private String notes;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}