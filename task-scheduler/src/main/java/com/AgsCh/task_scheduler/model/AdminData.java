package com.AgsCh.task_scheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_data")
public class AdminData {

    @Id
    private Long id;

    @OneToOne
    @MapsId // usa el mismo ID que User
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String orden; // Orden I, Orden II

    @Column(nullable = false)
    private String sedeResidencia;

    @Column(nullable = false)
    private String encargado;

    // getters y setters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;

        if (user != null && user.getAdminData() != this) {
            user.setAdminData(this);
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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