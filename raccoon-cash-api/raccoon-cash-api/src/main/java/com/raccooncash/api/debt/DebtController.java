package com.raccooncash.api.debt;

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
@RequestMapping("/api/debts")
public class DebtController {

    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    @GetMapping
    public ResponseEntity<List<DebtResponse>> getAllDebts() {
        return ResponseEntity.ok(debtService.getAllDebts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebtResponse> getDebtById(@PathVariable Long id) {
        return ResponseEntity.ok(debtService.getDebtById(id));
    }

    @PostMapping
    public ResponseEntity<DebtResponse> createDebt(@Valid @RequestBody DebtRequest request) {
        return ResponseEntity.ok(debtService.createDebt(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DebtResponse> updateDebt(@PathVariable Long id, @Valid @RequestBody DebtRequest request) {
        return ResponseEntity.ok(debtService.updateDebt(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebt(@PathVariable Long id) {
        debtService.deleteDebt(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<DebtPaymentResponse> addPayment(@PathVariable Long id, @Valid @RequestBody DebtPaymentRequest request) {
        return ResponseEntity.ok(debtService.addPayment(id, request));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<DebtPaymentResponse>> getPayments(@PathVariable Long id) {
        return ResponseEntity.ok(debtService.getPayments(id));
    }
}