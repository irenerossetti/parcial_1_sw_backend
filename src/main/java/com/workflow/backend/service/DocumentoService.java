package com.workflow.backend.service;

import com.workflow.backend.model.Documento;
import com.workflow.backend.model.Permiso;
import com.workflow.backend.model.TipoAcceso;
import com.workflow.backend.repository.DocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para Documentos
 * 
 * Métodos:
 * - crearDocumento(documento, inputStream): Crea documento en S3 y MongoDB
 * - obtenerDocumento(id): Obtiene documento por ID
 * - obtenerDocumentosPorTramite(tramiteId): Lista todos los documentos de un trámite
 * - actualizarDocumento(documento): Actualiza metadatos
 * - eliminarDocumento(id): Elimina documento y archivo de S3
 * - agregarPermiso(documentoId, usuarioId, accesos): Asigna permisos
 * - validarAcceso(documentoId, usuarioId, tipoAcceso): Verifica permisos
 */
@Service
@Transactional
public class DocumentoService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentoService.class);
    
    @Autowired
    private DocumentoRepository documentoRepository;
    
    @Autowired
    private S3Service s3Service;
    
    /**
     * Crea un nuevo documento
     * 1. Sube archivo a S3
     * 2. Guarda metadatos en MongoDB
     * 3. Asigna permisos iniciales
     * 
     * @param documento Documento con metadatos
     * @param inputStream Stream del archivo
     * @param usuarioId Usuario que crea el documento
     * @return Documento creado
     */
    public Documento crearDocumento(Documento documento, InputStream inputStream, String usuarioId) {
        try {
            logger.info("📄 Creando documento: {}", documento.getNombreArchivo());
            
            // Construir s3Key: tramites/{tramiteId}/documentos/{nombreArchivo}
            String s3Key = String.format("tramites/%s/documentos/%s-%d",
                documento.getTramiteId(),
                documento.getNombreArchivo(),
                System.currentTimeMillis()
            );
            
            // Subir a S3
            Map<String, String> uploadResult = s3Service.uploadFile(
                s3Key,
                inputStream,
                documento.getTipo()
            );
            
            // Actualizar documento con información de S3
            documento.setS3Key(s3Key);
            documento.setS3Url(uploadResult.get("url"));
            documento.setVersion(1);
            documento.setEstado("PENDIENTE");
            documento.setCreadoPor(usuarioId);
            documento.setCreadoEn(LocalDateTime.now());
            documento.setModificadoPor(usuarioId);
            documento.setModificadoEn(LocalDateTime.now());
            
            // Asignar permisos iniciales al usuario que lo crea
            Permiso permisoInicial = new Permiso(usuarioId,
                TipoAcceso.READ,
                TipoAcceso.WRITE,
                TipoAcceso.DELETE,
                TipoAcceso.SHARE
            );
            permisoInicial.setAsignadoPor(usuarioId);
            permisoInicial.setAsignadoEn(LocalDateTime.now());
            documento.agregarPermiso(permisoInicial);
            
            // Guardar en MongoDB
            Documento documentoGuardado = documentoRepository.save(documento);
            
            logger.info("✓ Documento creado exitosamente: ID={}", documentoGuardado.getId());
            return documentoGuardado;
            
        } catch (Exception e) {
            logger.error("❌ Error creando documento: {}", documento.getNombreArchivo(), e);
            throw new RuntimeException("Error creating document: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene un documento por ID
     * 
     * @param documentoId ID del documento
     * @return Documento
     */
    public Documento obtenerDocumento(String documentoId) {
        return documentoRepository.findById(documentoId)
            .orElseThrow(() -> {
                logger.error("❌ Documento no encontrado: {}", documentoId);
                return new RuntimeException("Document not found: " + documentoId);
            });
    }
    
    /**
     * Obtiene todos los documentos de un trámite
     * 
     * @param tramiteId ID del trámite
     * @return Lista de documentos
     */
    public List<Documento> obtenerDocumentosPorTramite(String tramiteId) {
        List<Documento> documentos = documentoRepository.findByTramiteId(tramiteId);
        logger.info("📋 Documentos encontrados para trámite {}: {}", tramiteId, documentos.size());
        return documentos;
    }
    
    /**
     * Obtiene documentos filtrados por estado
     * 
     * @param tramiteId ID del trámite
     * @param estado Estado a filtrar
     * @return Lista de documentos
     */
    public List<Documento> obtenerDocumentosPorEstado(String tramiteId, String estado) {
        return documentoRepository.findByTramiteIdAndEstado(tramiteId, estado);
    }
    
    /**
     * Actualiza un documento
     * 
     * @param documento Documento con cambios
     * @return Documento actualizado
     */
    public Documento actualizarDocumento(Documento documento, String usuarioId) {
        try {
            Documento existente = obtenerDocumento(documento.getId());
            
            // Actualizar campos
            existente.setEstado(documento.getEstado());
            existente.setMetadatos(documento.getMetadatos());
            existente.setModificadoPor(usuarioId);
            existente.setModificadoEn(LocalDateTime.now());
            
            Documento actualizado = documentoRepository.save(existente);
            logger.info("✓ Documento actualizado: {}", documento.getId());
            
            return actualizado;
            
        } catch (Exception e) {
            logger.error("❌ Error actualizando documento: {}", documento.getId(), e);
            throw new RuntimeException("Error updating document: " + e.getMessage());
        }
    }
    
    /**
     * Elimina un documento (de MongoDB y S3)
     * 
     * @param documentoId ID del documento
     */
    public void eliminarDocumento(String documentoId) {
        try {
            Documento documento = obtenerDocumento(documentoId);
            
            // Eliminar de S3
            s3Service.deleteFile(documento.getS3Key());
            
            // Eliminar de MongoDB
            documentoRepository.deleteById(documentoId);
            
            logger.info("✓ Documento eliminado: {}", documentoId);
            
        } catch (Exception e) {
            logger.error("❌ Error eliminando documento: {}", documentoId, e);
            throw new RuntimeException("Error deleting document: " + e.getMessage());
        }
    }
    
    /**
     * Agrega un permiso a un documento
     * 
     * @param documentoId ID del documento
     * @param usuarioId ID del usuario que obtiene permiso
     * @param tiposAcceso Tipos de acceso a otorgar
     * @param asignadoPor Usuario que asigna el permiso
     */
    public void agregarPermiso(String documentoId, String usuarioId, 
                              List<TipoAcceso> tiposAcceso, String asignadoPor) {
        try {
            Documento documento = obtenerDocumento(documentoId);
            
            // Verificar si el usuario ya tiene permisos
            Optional<Permiso> permisoExistente = documento.getPermisos().stream()
                .filter(p -> p.getUsuarioId().equals(usuarioId))
                .findFirst();
            
            if (permisoExistente.isPresent()) {
                // Agregar nuevos accesos
                Permiso permiso = permisoExistente.get();
                for (TipoAcceso tipo : tiposAcceso) {
                    permiso.agregarAcceso(tipo);
                }
            } else {
                // Crear nuevo permiso
                Permiso nuevoPermiso = new Permiso(usuarioId);
                for (TipoAcceso tipo : tiposAcceso) {
                    nuevoPermiso.agregarAcceso(tipo);
                }
                nuevoPermiso.setAsignadoPor(asignadoPor);
                nuevoPermiso.setAsignadoEn(LocalDateTime.now());
                documento.agregarPermiso(nuevoPermiso);
            }
            
            documentoRepository.save(documento);
            logger.info("✓ Permisos agregados a documento {}: usuario={}", documentoId, usuarioId);
            
        } catch (Exception e) {
            logger.error("❌ Error agregando permiso: {}", documentoId, e);
            throw new RuntimeException("Error adding permission: " + e.getMessage());
        }
    }
    
    /**
     * Remueve un permiso de un documento
     * 
     * @param documentoId ID del documento
     * @param usuarioId ID del usuario
     * @param tipoAcceso Tipo de acceso a remover
     */
    public void removerAcceso(String documentoId, String usuarioId, TipoAcceso tipoAcceso) {
        try {
            Documento documento = obtenerDocumento(documentoId);
            
            documento.getPermisos().stream()
                .filter(p -> p.getUsuarioId().equals(usuarioId))
                .forEach(p -> p.removerAcceso(tipoAcceso));
            
            documentoRepository.save(documento);
            logger.info("✓ Acceso removido del documento {}: usuario={}, tipo={}", 
                documentoId, usuarioId, tipoAcceso);
            
        } catch (Exception e) {
            logger.error("❌ Error removiendo acceso: {}", documentoId, e);
            throw new RuntimeException("Error removing access: " + e.getMessage());
        }
    }
    
    /**
     * Valida si un usuario tiene acceso a un documento
     * 
     * @param documentoId ID del documento
     * @param usuarioId ID del usuario
     * @param tipoAcceso Tipo de acceso requerido
     * @return true si tiene acceso
     */
    public boolean validarAcceso(String documentoId, String usuarioId, TipoAcceso tipoAcceso) {
        try {
            Documento documento = obtenerDocumento(documentoId);
            boolean tieneAcceso = documento.tieneAcceso(usuarioId, tipoAcceso);
            
            if (!tieneAcceso) {
                logger.warn("⚠️ Acceso denegado: usuario={}, documento={}, tipo={}", 
                    usuarioId, documentoId, tipoAcceso);
            }
            
            return tieneAcceso;
            
        } catch (Exception e) {
            logger.error("❌ Error validando acceso: {}", documentoId, e);
            return false;
        }
    }
    
    /**
     * Obtiene URL presignada para descargar un documento
     * (sin necesidad de credenciales AWS)
     * 
     * @param documentoId ID del documento
     * @param durationMinutes Duración de la URL en minutos
     * @return URL presignada
     */
    public String obtenerUrlDescarga(String documentoId, int durationMinutes) {
        try {
            Documento documento = obtenerDocumento(documentoId);
            String url = s3Service.generatePresignedUrl(documento.getS3Key(), durationMinutes);
            logger.info("✓ URL presignada generada para documento: {}", documentoId);
            return url;
        } catch (Exception e) {
            logger.error("❌ Error generando URL presignada: {}", documentoId, e);
            throw new RuntimeException("Error generating presigned URL: " + e.getMessage());
        }
    }
}
