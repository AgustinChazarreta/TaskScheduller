package com.AgsCh.task_scheduler.dto.request;

public class AdminRegisterRequest {

    private String nombre;
    private String email;
    private String password;

    private String orden; // Orden I / Orden II
    private String sedeResidencia;
    private String encargado;

    // getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrden() {
        return orden;
    }

    public void setOrden(String orden) {
        this.orden = orden;
    }

    public String getSedeResidencia() {
        return sedeResidencia;
    }

    public void setSedeResidencia(String sedeResidencia) {
        this.sedeResidencia = sedeResidencia;
    }

    public String getEncargado() {
        return encargado;
    }

    public void setEncargado(String encargado) {
        this.encargado = encargado;
    }
}