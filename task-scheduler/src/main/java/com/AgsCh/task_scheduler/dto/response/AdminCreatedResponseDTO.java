package com.AgsCh.task_scheduler.dto.response;

import java.time.format.DateTimeFormatter;

import com.AgsCh.task_scheduler.model.User;

public class AdminCreatedResponseDTO {

    private Long adminId;
    private String username;
    private boolean active;
    private Long houseId;
    private String houseName;
    private String createdAt; // fecha formateada
    private String temporaryPassword; // opcional, solo al crear

    // Constructor para listar admins
    public AdminCreatedResponseDTO(User user) {
        this.adminId = user.getId();
        this.username = user.getUsername();
        this.active = user.isActive();
        this.houseId = user.getHouse().getId();
        this.houseName = user.getHouse().getName();
        // Formateo la fecha como en tu JSON
        if (user.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");
            this.createdAt = user.getCreatedAt().format(formatter);
        } else {
            this.createdAt = null;
        }
        this.temporaryPassword = null; // solo se llena al crear
    }

    // Constructor para crear admin y devolver contraseña temporal
    public AdminCreatedResponseDTO(User user, String temporaryPassword) {
        this(user); // llama al constructor anterior
        this.temporaryPassword = temporaryPassword;
    }

    // Getters
    public Long getAdminId() {
        return adminId;
    }

    public String getUsername() {
        return username;
    }

    public boolean isActive() {
        return active;
    }

    public Long getHouseId() {
        return houseId;
    }

    public String getHouseName() {
        return houseName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }
}