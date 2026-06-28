package com.raccooncash.api.usuario;

public class AuthRespuesta {
    private Long id;
    private String nombre;
    private String correo;

    public AuthRespuesta() {
    }

    public AuthRespuesta(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.correo = usuario.getCorreo();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
