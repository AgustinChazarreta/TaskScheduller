package com.AgsCh.task_scheduler.dto.request;

public class CreateWebmasterRequestDTO {
    private String username;
    private String nombre;
    private boolean active;

    public CreateWebmasterRequestDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}