package com.workflow.backend.models;

import java.time.LocalDateTime;

public class Movimiento {
    private String departamento;
    private LocalDateTime fecha;
    private String observacion;
    private String usuario;

    public Movimiento() {
        this.fecha = LocalDateTime.now();
    }

    public Movimiento(String departamento, String observacion, String usuario) {
        this.departamento = departamento;
        this.observacion = observacion;
        this.usuario = usuario;
        this.fecha = LocalDateTime.now();
    }

    // Getters y Setters
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}