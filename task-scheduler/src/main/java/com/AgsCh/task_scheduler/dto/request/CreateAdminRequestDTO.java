package com.AgsCh.task_scheduler.dto.request;

public class CreateAdminRequestDTO {
    private String nombre;
    private String username;
    private String orden;
    private String sedeResidencia;
    private String encargado;

    private boolean active;
    private Long houseId;

    public CreateAdminRequestDTO() {}
    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }
}

