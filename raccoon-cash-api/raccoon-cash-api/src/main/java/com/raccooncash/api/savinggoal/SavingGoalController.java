package com.raccooncash.api.savinggoal;

import com.raccooncash.api.transaccion.TransaccionRespuesta;
import com.raccooncash.api.transaccion.TransaccionServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saving-goals")
public class SavingGoalController {

    @Autowired
    private SavingGoalService savingGoalService;

    @Autowired
    private TransaccionServicio transaccionServicio;

    @GetMapping
    public List<SavingGoalResponse> getAllSavingGoals(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        return savingGoalService.getAllSavingGoals(usuarioId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingGoalResponse> getSavingGoalById(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                                @PathVariable Long id) {
        return savingGoalService.getSavingGoalById(usuarioId, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SavingGoalResponse> createSavingGoal(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                               @Valid @RequestBody SavingGoal savingGoal) {
        return ResponseEntity.ok(savingGoalService.createSavingGoal(usuarioId, savingGoal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingGoalResponse> updateSavingGoal(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                               @PathVariable Long id,
                                                               @Valid @RequestBody SavingGoal savingGoalDetails) {
        return ResponseEntity.ok(savingGoalService.updateSavingGoal(usuarioId, id, savingGoalDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavingGoal(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                 @PathVariable Long id) {
        savingGoalService.deleteSavingGoal(usuarioId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransaccionRespuesta>> getTransactionsBySavingGoalId(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                                                    @PathVariable Long id) {
        List<TransaccionRespuesta> transactions = transaccionServicio.getTransactionsBySavingGoalId(usuarioId, id);
        return ResponseEntity.ok(transactions);
    }
}
