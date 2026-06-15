package com.raccooncash.api.deuda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PagoDeudaRespuesta {
    private Long id;
    private Long debtId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String notes;
    private LocalDateTime createdAt;

    public PagoDeudaRespuesta() {
    }

    public PagoDeudaRespuesta(PagoDeuda payment) {
        this.id = payment.getId();
        this.debtId = payment.getDebt().getId();
        this.amount = payment.getAmount();
        this.paymentDate = payment.getPaymentDate();
        this.notes = payment.getNotes();
        this.createdAt = payment.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDebtId() {
        return debtId;
    }

    public void setDebtId(Long debtId) {
        this.debtId = debtId;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}