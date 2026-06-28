package com.raccooncash.api.cuenta;

import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
import com.raccooncash.api.usuario.Usuario;
import com.raccooncash.api.usuario.UsuarioServicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuentaServicio {

    private final CuentaRepositorio accountRepository;
    private final UsuarioServicio usuarioServicio;

    public CuentaServicio(CuentaRepositorio accountRepository, UsuarioServicio usuarioServicio) {
        this.accountRepository = accountRepository;
        this.usuarioServicio = usuarioServicio;
    }

    public List<CuentaRespuesta> getAllActiveAccounts(Long usuarioId) {
        return accountRepository.findAllByUsuarioIdAndActiveTrue(usuarioId)
                .stream()
                .map(CuentaRespuesta::new)
                .collect(Collectors.toList());
    }

    public CuentaRespuesta getAccountById(Long usuarioId, Long id) {
        Cuenta account = accountRepository.findByIdAndUsuarioIdAndActiveTrue(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada"));
        return new CuentaRespuesta(account);
    }

    @Transactional
    public CuentaRespuesta createAccount(Long usuarioId, CuentaSolicitud request) {
        Usuario usuario = usuarioServicio.obtenerUsuario(usuarioId);
        Cuenta account = new Cuenta();
        account.setUsuario(usuario);
        account.setName(request.getName());
        account.setType(request.getType());
        account.setInitialBalance(request.getInitialBalance());
        account.setCurrentBalance(request.getInitialBalance());
        account.setCurrency(request.getCurrency());
        account.setDecimalPrecision(request.getDecimalPrecision());
        account.setColor(request.getColor());
        account.setActive(true);

        Cuenta savedAccount = accountRepository.save(account);
        return new CuentaRespuesta(savedAccount);
    }

    @Transactional
    public CuentaRespuesta updateAccount(Long usuarioId, Long id, CuentaSolicitud request) {
        Cuenta account = accountRepository.findByIdAndUsuarioIdAndActiveTrue(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada"));

        account.setName(request.getName());
        account.setType(request.getType());
        account.setInitialBalance(request.getInitialBalance());
        account.setCurrency(request.getCurrency());
        account.setDecimalPrecision(request.getDecimalPrecision());
        account.setColor(request.getColor());

        Cuenta updatedAccount = accountRepository.save(account);
        return new CuentaRespuesta(updatedAccount);
    }

    @Transactional
    public void deleteAccount(Long usuarioId, Long id) {
        Cuenta account = accountRepository.findByIdAndUsuarioIdAndActiveTrue(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada"));

        account.setActive(false);
        accountRepository.save(account);
    }
}
