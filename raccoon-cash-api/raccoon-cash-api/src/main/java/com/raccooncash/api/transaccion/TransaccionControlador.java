package com.raccooncash.api.transaccion;

import jakarta.validation.Valid;
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
@RequestMapping("/api/transactions")
public class TransaccionControlador {

    private final TransaccionServicio transactionService;

    public TransaccionControlador(TransaccionServicio transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransaccionRespuesta>> getTransactions(
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TipoTransaccion type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(transactionService.getTransactions(usuarioId, accountId, categoryId, type, from, to));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransaccionRespuesta> getTransactionById(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                                   @PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(usuarioId, id));
    }

    @PostMapping
    public ResponseEntity<TransaccionRespuesta> createTransaction(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                                  @Valid @RequestBody TransaccionSolicitud request) {
        return ResponseEntity.ok(transactionService.createTransaction(usuarioId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransaccionRespuesta> updateTransaction(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                                  @PathVariable Long id,
                                                                  @Valid @RequestBody TransaccionSolicitud request) {
        return ResponseEntity.ok(transactionService.updateTransaction(usuarioId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                  @PathVariable Long id) {
        transactionService.deleteTransaction(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
