package com.workflow.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase Permiso (embedded en Documento)
 * Controla quién puede hacer qué con cada documento
 * 
 * Ejemplo:
 * {
 *   "usuarioId": "user123",
 *   "tiposAcceso": ["READ", "WRITE"],
 *   "asignadoPor": "admin@email.com",
 *   "asignadoEn": "2024-06-05T10:30:00"
 * }
 */
public class Permiso {
    
    private String usuarioId;                   // ID del usuario
    private List<TipoAcceso> tiposAcceso;       // READ, WRITE, DELETE, SHARE, UPLOAD
    private String asignadoPor;                 // Usuario que asignó el permiso
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime asignadoEn;
    
    // ==========================================================================
    // CONSTRUCTORES
    // ==========================================================================
    
    public Permiso() {
        this.tiposAcceso = new ArrayList<>();
    }
    
    public Permiso(String usuarioId) {
        this();
        this.usuarioId = usuarioId;
    }
    
    public Permiso(String usuarioId, TipoAcceso... tipos) {
        this(usuarioId);
        for (TipoAcceso tipo : tipos) {
            this.tiposAcceso.add(tipo);
        }
    }
    
    // ==========================================================================
    // MÉTODOS
    // ==========================================================================
    
    public void agregarAcceso(TipoAcceso tipoAcceso) {
        if (!this.tiposAcceso.contains(tipoAcceso)) {
            this.tiposAcceso.add(tipoAcceso);
        }
    }
    
    public void removerAcceso(TipoAcceso tipoAcceso) {
        this.tiposAcceso.remove(tipoAcceso);
    }
    
    public boolean tiene(TipoAcceso tipoAcceso) {
        return this.tiposAcceso.contains(tipoAcceso);
    }
    
    // ==========================================================================
    // GETTERS Y SETTERS
    // ==========================================================================
    
    public String getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public List<TipoAcceso> getTiposAcceso() {
        return tiposAcceso;
    }
    
    public void setTiposAcceso(List<TipoAcceso> tiposAcceso) {
        this.tiposAcceso = tiposAcceso;
    }
    
    public String getAsignadoPor() {
        return asignadoPor;
    }
    
    public void setAsignadoPor(String asignadoPor) {
        this.asignadoPor = asignadoPor;
    }
    
    public LocalDateTime getAsignadoEn() {
        return asignadoEn;
    }
    
    public void setAsignadoEn(LocalDateTime asignadoEn) {
        this.asignadoEn = asignadoEn;
    }
}
