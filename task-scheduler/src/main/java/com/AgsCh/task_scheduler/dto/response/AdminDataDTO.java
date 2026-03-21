package com.AgsCh.task_scheduler.dto.response;

import com.AgsCh.task_scheduler.model.AdminData;

public class AdminDataDTO {

    private String nombre;
    private String orden;
    private String sedeResidencia;
    private String encargado;

    // Constructor desde la entidad AdminData
    public AdminDataDTO(AdminData adminData) {
        if (adminData != null) {
            this.nombre = adminData.getNombre();
            this.orden = adminData.getOrden();
            this.sedeResidencia = adminData.getSedeResidencia();
            this.encargado = adminData.getEncargado();
        }
    }

    // Getters
    public String getNombre() {
        return nombre;
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