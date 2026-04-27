package com.workflow.backend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;
    private String email;
    private String password;
    private String nombre;
    private String rol; // ADMIN, FUNCIONARIO, CLIENTE
    private String departamentoId;  // ← NUEVO: ID del departamento al que pertenece
    private String departamentoNombre; // ← NUEVO: Nombre del departamento
    private boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getDepartamentoId() { return departamentoId; }
    public void setDepartamentoId(String departamentoId) { this.departamentoId = departamentoId; }

    public String getDepartamentoNombre() { return departamentoNombre; }
    public void setDepartamentoNombre(String departamentoNombre) { this.departamentoNombre = departamentoNombre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}