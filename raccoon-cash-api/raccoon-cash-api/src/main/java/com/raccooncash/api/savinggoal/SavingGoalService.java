package com.raccooncash.api.savinggoal;

import com.raccooncash.api.transaccion.Transaccion;
import com.raccooncash.api.transaccion.TransaccionRepositorio;
import com.raccooncash.api.usuario.Usuario;
import com.raccooncash.api.usuario.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SavingGoalService {

    @Autowired
    private SavingGoalRepository savingGoalRepository;

    @Autowired
    private TransaccionRepositorio transaccionRepositorio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    public List<SavingGoalResponse> getAllSavingGoals(Long usuarioId) {
        return savingGoalRepository.findAllByUsuarioId(usuarioId).stream()
                .map(savingGoal -> convertToDto(savingGoal, usuarioId))
                .collect(Collectors.toList());
    }

    public Optional<SavingGoalResponse> getSavingGoalById(Long usuarioId, Long id) {
        return savingGoalRepository.findByIdAndUsuarioId(id, usuarioId)
                .map(savingGoal -> convertToDto(savingGoal, usuarioId));
    }

    public SavingGoal createSavingGoal(Long usuarioId, SavingGoal savingGoal) {
        Usuario usuario = usuarioServicio.obtenerUsuario(usuarioId);
        savingGoal.setUsuario(usuario);
        return savingGoalRepository.save(savingGoal);
    }

    @Transactional
    public SavingGoal updateSavingGoal(Long usuarioId, Long id, SavingGoal savingGoalDetails) {
        SavingGoal savingGoal = savingGoalRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("SavingGoal not found with id " + id));

        savingGoal.setName(savingGoalDetails.getName());
        savingGoal.setTargetAmount(savingGoalDetails.getTargetAmount());
        savingGoal.setDeadline(savingGoalDetails.getDeadline());
        savingGoal.setColor(savingGoalDetails.getColor());
        savingGoal.setIcon(savingGoalDetails.getIcon());
        savingGoal.setCurrency(savingGoalDetails.getCurrency());

        return savingGoalRepository.save(savingGoal);
    }

    public void deleteSavingGoal(Long usuarioId, Long id) {
        SavingGoal savingGoal = savingGoalRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RuntimeException("SavingGoal not found with id " + id));
        savingGoalRepository.delete(savingGoal);
    }

    private SavingGoalResponse convertToDto(SavingGoal savingGoal, Long usuarioId) {
        List<Transaccion> activeTransactions = transaccionRepositorio.findBySavingGoalAndUsuarioIdAndActiveTrue(savingGoal, usuarioId);

        Double currentAmount = activeTransactions.stream()
                .mapToDouble(transaccion -> transaccion.getAmount().doubleValue())
                .sum();
        int transactionCount = activeTransactions.size();

        return new SavingGoalResponse(
                savingGoal.getId(),
                savingGoal.getName(),
                savingGoal.getTargetAmount(),
                currentAmount,
                savingGoal.getDeadline(),
                savingGoal.getColor(),
                savingGoal.getIcon(),
                savingGoal.getCurrency(),
                transactionCount
        );
    }
}
