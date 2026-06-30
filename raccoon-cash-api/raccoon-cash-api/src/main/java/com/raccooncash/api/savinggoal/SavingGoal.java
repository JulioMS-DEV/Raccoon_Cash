package com.raccooncash.api.savinggoal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raccooncash.api.transaccion.Transaccion;
import com.raccooncash.api.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "saving_goal")
public class SavingGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank(message = "El nombre de la meta es obligatorio")
    private String name;

    @NotNull(message = "El monto objetivo es obligatorio")
    @Positive(message = "El monto objetivo debe ser mayor a cero")
    private Double targetAmount;

    @NotNull(message = "La fecha limite es obligatoria")
    private LocalDate deadline;

    @NotBlank(message = "El color es obligatorio")
    private String color;
    private String icon;
    private String currency;

    @OneToMany(mappedBy = "savingGoal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaccion> transactions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Set<Transaccion> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transaccion> transactions) {
        this.transactions = transactions;
    }
}
