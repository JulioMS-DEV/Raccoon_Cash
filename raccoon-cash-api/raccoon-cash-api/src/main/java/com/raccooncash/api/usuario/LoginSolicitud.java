package com.raccooncash.api.usuario;

import jakarta.validation.constraints.NotBlank;

public class LoginSolicitud {

    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @NotBlank(message = "El password es obligatorio")
    private String password;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
