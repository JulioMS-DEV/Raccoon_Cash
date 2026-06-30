package com.raccooncash.api.savinggoal;

import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
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

    @Transactional
    public SavingGoalResponse createSavingGoal(Long usuarioId, SavingGoal savingGoal) {
        Usuario usuario = usuarioServicio.obtenerUsuario(usuarioId);
        savingGoal.setUsuario(usuario);
        savingGoal.setName(savingGoal.getName().trim());
        savingGoal.setCurrency(defaultCurrency(savingGoal.getCurrency()));
        SavingGoal savedSavingGoal = savingGoalRepository.save(savingGoal);
        return convertToDto(savedSavingGoal, usuarioId);
    }

    @Transactional
    public SavingGoalResponse updateSavingGoal(Long usuarioId, Long id, SavingGoal savingGoalDetails) {
        SavingGoal savingGoal = savingGoalRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Meta de ahorro no encontrada"));

        savingGoal.setName(savingGoalDetails.getName().trim());
        savingGoal.setTargetAmount(savingGoalDetails.getTargetAmount());
        savingGoal.setDeadline(savingGoalDetails.getDeadline());
        savingGoal.setColor(savingGoalDetails.getColor());
        savingGoal.setIcon(savingGoalDetails.getIcon());
        savingGoal.setCurrency(defaultCurrency(savingGoalDetails.getCurrency()));

        return convertToDto(savingGoalRepository.save(savingGoal), usuarioId);
    }

    public void deleteSavingGoal(Long usuarioId, Long id) {
        SavingGoal savingGoal = savingGoalRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Meta de ahorro no encontrada"));
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
                defaultCurrency(savingGoal.getCurrency()),
                transactionCount
        );
    }

    private String defaultCurrency(String currency) {
        return currency == null || currency.isBlank() ? "C$" : currency;
    }
}
