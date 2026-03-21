package com.AgsCh.task_scheduler.dto.response;

import com.AgsCh.task_scheduler.model.User;

public class AdminPendingResponseDTO {

    private Long id;
    private String email;
    private boolean active;

    private String nombre;
    private String orden;
    private String sedeResidencia;
    private String encargado;

    private Long houseId;

    public AdminPendingResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getUsername();
        this.active = user.isActive();

        if (user.getAdminData() != null) {
            this.nombre = user.getAdminData().getNombre();
            this.orden = user.getAdminData().getOrden();
            this.sedeResidencia = user.getAdminData().getSedeResidencia();
            this.encargado = user.getAdminData().getEncargado();
        }

        if (user.getHouse() != null) {
            this.houseId = user.getHouse().getId();
        }
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActive() {
        return active;
    }

    public String getEmail() {
        return email;
    }

    public Long getHouseId() {
        return houseId;
    }

    public String getOrden() {
        return orden;
    }

    public String getSedeResidencia() {
        return sedeResidencia;
    }

    public String getEncargado() {
        return encargado;
    }
}