package com.raccooncash.api.cuenta;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class CuentaControlador {

    private final CuentaServicio accountService;

    public CuentaControlador(CuentaServicio accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<CuentaRespuesta>> getAllAccounts(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(accountService.getAllActiveAccounts(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaRespuesta> getAccountById(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(usuarioId, id));
    }

    @PostMapping
    public ResponseEntity<CuentaRespuesta> createAccount(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                         @Valid @RequestBody CuentaSolicitud request) {
        return ResponseEntity.ok(accountService.createAccount(usuarioId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaRespuesta> updateAccount(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody CuentaSolicitud request) {
        return ResponseEntity.ok(accountService.updateAccount(usuarioId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                              @PathVariable Long id) {
        accountService.deleteAccount(usuarioId, id);
        return ResponseEntity.noContent().build();
    }
}
