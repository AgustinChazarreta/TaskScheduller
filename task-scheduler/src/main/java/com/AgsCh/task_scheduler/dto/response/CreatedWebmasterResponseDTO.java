package com.AgsCh.task_scheduler.dto.response;

import com.AgsCh.task_scheduler.model.User;

public class CreatedWebmasterResponseDTO {

    private Long id;
    private String username;
    private String nombre;
    private boolean active;
    private String temporaryPassword;

    public CreatedWebmasterResponseDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.nombre = user.getAdminData() != null
                ? user.getAdminData().getNombre()
                : null;
        this.active = user.isActive();
    }

    public CreatedWebmasterResponseDTO(User user, String temporaryPassword) {
        this(user);
        this.temporaryPassword = temporaryPassword;
    }

    // getters

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActive() {
        return active;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }
}