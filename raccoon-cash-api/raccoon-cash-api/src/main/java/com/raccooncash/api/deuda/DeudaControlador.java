package com.raccooncash.api.deuda;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/debts")
public class DeudaControlador {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeudaControlador.class);

    private final DeudaServicio debtService;

    public DeudaControlador(DeudaServicio debtService) {
        this.debtService = debtService;
    }

    @GetMapping
    public ResponseEntity<List<DeudaRespuesta>> getAllDebts(
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestParam(required = false) TipoDeuda type,
            @RequestParam(required = false) EstadoDeuda status,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(debtService.getAllDebts(usuarioId, type, status, accountId, dueFrom, dueTo, overdue, search));
    }

    @GetMapping("/reminders")
    public ResponseEntity<List<DeudaRespuesta>> getPendingReminders(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(debtService.getPendingReminders(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeudaRespuesta> getDebtById(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                      @PathVariable Long id) {
        return ResponseEntity.ok(debtService.getDebtById(usuarioId, id));
    }

    @PostMapping
    public ResponseEntity<DeudaRespuesta> createDebt(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                     @Valid @RequestBody DeudaSolicitud request) {
        return ResponseEntity.ok(debtService.createDebt(usuarioId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeudaRespuesta> updateDebt(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody DeudaSolicitud request) {
        return ResponseEntity.ok(debtService.updateDebt(usuarioId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebt(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                           @PathVariable Long id) {
        debtService.deleteDebt(usuarioId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<PagoDeudaRespuesta> addPayment(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                          @PathVariable Long id,
                                                          @Valid @RequestBody PagoDeudaSolicitud request) {
        LOGGER.info("Debt payment request usuarioId={}, debtId={}, amount={}, paymentDate={}, accountId={}",
                usuarioId,
                id,
                request.getAmount(),
                request.getPaymentDate(),
                request.getAccountId());
        return ResponseEntity.ok(debtService.addPayment(usuarioId, id, request));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<PagoDeudaRespuesta>> getPayments(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                                @PathVariable Long id) {
        return ResponseEntity.ok(debtService.getPayments(usuarioId, id));
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                              @PathVariable Long id,
                                              @PathVariable Long paymentId) {
        debtService.deletePayment(usuarioId, id, paymentId);
        return ResponseEntity.noContent().build();
    }
}
