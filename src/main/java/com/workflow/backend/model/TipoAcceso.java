package com.workflow.backend.model;

/**
 * Enum de tipos de acceso a documentos
 * 
 * READ   = Descargar/Ver documento
 * WRITE  = Editar contenido del documento
 * UPLOAD = Subir nuevas versiones
 * DELETE = Eliminar documento
 * SHARE  = Compartir con otros usuarios
 */
public enum TipoAcceso {
    READ,       // Leer/Descargar
    WRITE,      // Editar
    UPLOAD,     // Subir versión
    DELETE,     // Eliminar
    SHARE       // Compartir con otros
}
