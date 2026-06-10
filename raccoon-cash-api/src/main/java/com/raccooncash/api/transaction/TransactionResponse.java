package com.raccooncash.api.transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class TransactionResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDateTime date;
    private TransactionType type;
    private Long accountId;
    private String accountName;
    private Long destinationAccountId;
    private String destinationAccountName;
    private Long toAccountId;
    private String toAccountName;
    private Long categoryId;
    private String categoryName;
    private String notes;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public TransactionResponse() {
    }
    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.description = transaction.getDescription();
        this.amount = transaction.getAmount();
        this.date = transaction.getDate();
        this.type = transaction.getType();
        this.accountId = transaction.getAccount().getId();
        this.accountName = transaction.getAccount().getName();
        this.notes = transaction.getNotes();
        this.active = transaction.getActive();
        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
        if (transaction.getToAccount() != null) {
            this.destinationAccountId = transaction.getToAccount().getId();
            this.destinationAccountName = transaction.getToAccount().getName();
            this.toAccountId = this.destinationAccountId;
            this.toAccountName = this.destinationAccountName;
        }
        if (transaction.getCategory() != null) {
            this.categoryId = transaction.getCategory().getId();
            this.categoryName = transaction.getCategory().getName();
        }
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    public TransactionType getType() {
        return type;
    }
    public void setType(TransactionType type) {
        this.type = type;
    }
    public Long getAccountId() {
        return accountId;
    }
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    public String getAccountName() {
        return accountName;
    }
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    public Long getDestinationAccountId() {
        return destinationAccountId;
    }
    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }
    public String getDestinationAccountName() {
        return destinationAccountName;
    }
    public void setDestinationAccountName(String destinationAccountName) {
        this.destinationAccountName = destinationAccountName;
    }
    public Long getToAccountId() {
        return toAccountId;
    }
    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }
    public String getToAccountName() {
        return toAccountName;
    }
    public void setToAccountName(String toAccountName) {
        this.toAccountName = toAccountName;
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
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
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
