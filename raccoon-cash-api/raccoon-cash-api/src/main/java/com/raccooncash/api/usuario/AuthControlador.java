package com.raccooncash.api.usuario;

import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

    private final UsuarioRepositorio usuarioRepositorio;

    public AuthControlador(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthRespuesta> registrar(@Valid @RequestBody RegistroSolicitud request) {
        String correo = normalizarCorreo(request.getCorreo());
        if (usuarioRepositorio.existsByCorreo(correo)) {
            throw new SolicitudIncorrectaException("El correo ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(request.getPassword());

        return ResponseEntity.ok(new AuthRespuesta(usuarioRepositorio.save(usuario)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRespuesta> login(@Valid @RequestBody LoginSolicitud request) {
        String correo = normalizarCorreo(request.getCorreo());
        Usuario usuario = usuarioRepositorio.findByCorreo(correo)
                .filter(foundUser -> foundUser.getPassword().equals(request.getPassword()))
                .orElseThrow(() -> new SolicitudIncorrectaException("Correo o password incorrectos"));

        return ResponseEntity.ok(new AuthRespuesta(usuario));
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
