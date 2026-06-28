package com.raccooncash.api.savinggoal;

import com.raccooncash.api.transaccion.TransaccionRespuesta;
import com.raccooncash.api.transaccion.TransaccionServicio;
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
    public SavingGoal createSavingGoal(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                       @RequestBody SavingGoal savingGoal) {
        return savingGoalService.createSavingGoal(usuarioId, savingGoal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingGoal> updateSavingGoal(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                       @PathVariable Long id,
                                                       @RequestBody SavingGoal savingGoalDetails) {
        try {
            SavingGoal updatedSavingGoal = savingGoalService.updateSavingGoal(usuarioId, id, savingGoalDetails);
            return ResponseEntity.ok(updatedSavingGoal);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
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
