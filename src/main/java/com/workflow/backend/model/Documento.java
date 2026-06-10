package com.workflow.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entidad Documento
 * Representa archivos/documentos en el sistema
 * 
 * Estructura MongoDB:
 * {
 *   "_id": ObjectId,
 *   "tramiteId": "123",
 *   "nombreArchivo": "documento.pdf",
 *   "tipo": "application/pdf",
 *   "tamaño": 1024,
 *   "s3Url": "s3://bucket/tramites/123/documento.pdf",
 *   "s3Key": "tramites/123/documentos/documento.pdf",
 *   "version": 1,
 *   "permisos": [...],
 *   "metadatos": {...},
 *   "creadoPor": "user123",
 *   "creadoEn": "2024-06-05T10:30:00",
 *   "modificadoPor": "user123",
 *   "modificadoEn": "2024-06-05T10:30:00"
 * }
 */
@Document(collection = "documentos")
public class Documento {
    
    @Id
    private String id;
    
    private String tramiteId;           // ID del trámite al que pertenece
    private String nombreArchivo;       // Nombre original del archivo
    private String tipo;                // MIME type (application/pdf, image/png, etc)
    private Long tamaño;                // Tamaño en bytes
    
    private String s3Url;               // URL pública del archivo en S3
    private String s3Key;               // Ruta interna en S3 (para operaciones)
    
    private Integer version;            // Versión del documento (1, 2, 3...)
    private String estado;              // Estado: PENDIENTE, REVISIÓN, APROBADO, RECHAZADO
    
    private List<Permiso> permisos;     // Lista de permisos de acceso
    
    private Map<String, Object> metadatos;  // Metadatos personalizados (OCR, análisis, etc)
    
    private String creadoPor;           // Usuario que creó el documento
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime creadoEn;
    
    private String modificadoPor;       // Usuario que modificó por última vez
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modificadoEn;
    
    // ==========================================================================
    // CONSTRUCTORES
    // ==========================================================================
    
    public Documento() {
        this.permisos = new ArrayList<>();
        this.metadatos = new HashMap<>();
        this.version = 1;
        this.estado = "PENDIENTE";
    }
    
    public Documento(String tramiteId, String nombreArchivo, String tipo, Long tamaño) {
        this();
        this.tramiteId = tramiteId;
        this.nombreArchivo = nombreArchivo;
        this.tipo = tipo;
        this.tamaño = tamaño;
    }
    
    // ==========================================================================
    // MÉTODOS
    // ==========================================================================
    
    public void agregarPermiso(Permiso permiso) {
        if (this.permisos == null) {
            this.permisos = new ArrayList<>();
        }
        this.permisos.add(permiso);
    }
    
    public boolean tieneAcceso(String usuarioId, TipoAcceso tipoAcceso) {
        if (this.permisos == null) return false;
        
        return this.permisos.stream()
            .anyMatch(p -> p.getUsuarioId().equals(usuarioId) && 
                          p.getTiposAcceso().contains(tipoAcceso));
    }
    
    public void incrementarVersion() {
        this.version = (this.version != null ? this.version : 0) + 1;
    }
    
    // ==========================================================================
    // GETTERS Y SETTERS
    // ==========================================================================
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTramiteId() {
        return tramiteId;
    }
    
    public void setTramiteId(String tramiteId) {
        this.tramiteId = tramiteId;
    }
    
    public String getNombreArchivo() {
        return nombreArchivo;
    }
    
    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public Long getTamaño() {
        return tamaño;
    }
    
    public void setTamaño(Long tamaño) {
        this.tamaño = tamaño;
    }
    
    public String getS3Url() {
        return s3Url;
    }
    
    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }
    
    public String getS3Key() {
        return s3Key;
    }
    
    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public List<Permiso> getPermisos() {
        return permisos;
    }
    
    public void setPermisos(List<Permiso> permisos) {
        this.permisos = permisos;
    }
    
    public Map<String, Object> getMetadatos() {
        return metadatos;
    }
    
    public void setMetadatos(Map<String, Object> metadatos) {
        this.metadatos = metadatos;
    }
    
    public String getCreadoPor() {
        return creadoPor;
    }
    
    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }
    
    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
    
    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
    
    public String getModificadoPor() {
        return modificadoPor;
    }
    
    public void setModificadoPor(String modificadoPor) {
        this.modificadoPor = modificadoPor;
    }
    
    public LocalDateTime getModificadoEn() {
        return modificadoEn;
    }
    
    public void setModificadoEn(LocalDateTime modificadoEn) {
        this.modificadoEn = modificadoEn;
    }
}
