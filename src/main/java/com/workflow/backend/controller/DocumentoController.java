package com.workflow.backend.controller;

import com.workflow.backend.model.Documento;
import com.workflow.backend.model.TipoAcceso;
import com.workflow.backend.service.DocumentoService;
import com.workflow.backend.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.util.AntPathMatcher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.*;

/**
 * REST Controller para Documentos
 * 
 * Endpoints:
 * POST   /api/documentos/upload - Subir nuevo documento
 * GET    /api/documentos/{id} - Obtener documento
 * GET    /api/documentos/tramite/{tramiteId} - Listar documentos de un trámite
 * PUT    /api/documentos/{id} - Actualizar documento
 * DELETE /api/documentos/{id} - Eliminar documento
 * POST   /api/documentos/{id}/permisos - Agregar permiso
 * GET    /api/documentos/{id}/descarga - Generar URL para descarga
 */
@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "*")
public class DocumentoController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentoController.class);
    
    @Autowired
    private DocumentoService documentoService;
    
    @Autowired
    private S3Service s3Service;
    
    /**
     * POST /api/documentos/upload
     * Subir nuevo documento asociado a un trámite
     */
    @PostMapping("/upload")
    public ResponseEntity<Documento> uploadDocumento(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tramiteId") String tramiteId,
            @RequestParam("usuarioId") String usuarioId,
            @RequestParam(value = "nombreArchivo", required = false) String nombreArchivo,
            @RequestParam(value = "tipo", required = false) String tipo) {
        try {
            logger.info("📤 Recibiendo archivo {} para tramite {}", file.getOriginalFilename(), tramiteId);
            Documento documento = new Documento();
            documento.setTramiteId(tramiteId);
            documento.setNombreArchivo(nombreArchivo != null ? nombreArchivo : file.getOriginalFilename());
            documento.setTipo(tipo != null ? tipo : file.getContentType());
            documento.setTamaño(file.getSize());
            
            Documento creado = documentoService.crearDocumento(documento, file.getInputStream(), usuarioId);
            return ResponseEntity.ok(creado);
        } catch (Exception e) {
            logger.error("❌ Error en endpoint upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/documentos/{id}
     * Obtener metadatos de un documento por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Documento> obtenerDocumento(@PathVariable String id) {
        try {
            Documento documento = documentoService.obtenerDocumento(id);
            return ResponseEntity.ok(documento);
        } catch (Exception e) {
            logger.error("❌ Error al obtener documento {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * GET /api/documentos/tramite/{tramiteId}
     * Listar documentos asociados a un trámite
     */
    @GetMapping("/tramite/{tramiteId}")
    public ResponseEntity<List<Documento>> obtenerDocumentosPorTramite(@PathVariable String tramiteId) {
        try {
            List<Documento> documentos = documentoService.obtenerDocumentosPorTramite(tramiteId);
            return ResponseEntity.ok(documentos);
        } catch (Exception e) {
            logger.error("❌ Error al obtener documentos del tramite {}: {}", tramiteId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * PUT /api/documentos/{id}
     * Actualizar metadatos de un documento
     */
    @PutMapping("/{id}")
    public ResponseEntity<Documento> actualizarDocumento(
            @PathVariable String id,
            @RequestBody Documento documento,
            @RequestParam("usuarioId") String usuarioId) {
        try {
            documento.setId(id);
            Documento actualizado = documentoService.actualizarDocumento(documento, usuarioId);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            logger.error("❌ Error al actualizar documento {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * DELETE /api/documentos/{id}
     * Eliminar un documento por su ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarDocumento(@PathVariable String id) {
        try {
            documentoService.eliminarDocumento(id);
            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("message", "Documento eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error al eliminar documento {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/documentos/{id}/permisos
     * Agregar permisos de acceso a un documento para un usuario
     */
    @PostMapping("/{id}/permisos")
    public ResponseEntity<Map<String, String>> agregarPermiso(
            @PathVariable String id,
            @RequestParam("usuarioId") String usuarioId,
            @RequestParam("accesos") String accesos,
            @RequestParam("asignadoPor") String asignadoPor) {
        try {
            List<TipoAcceso> tiposAcceso = new ArrayList<>();
            for (String tipo : accesos.split(",")) {
                tiposAcceso.add(TipoAcceso.valueOf(tipo.trim().toUpperCase()));
            }
            
            documentoService.agregarPermiso(id, usuarioId, tiposAcceso, asignadoPor);
            
            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("message", "Permisos agregados correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error al agregar permisos: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/documentos/{id}/descarga
     * Generar URL prefirmada para descargar documento
     */
    @GetMapping("/{id}/descarga")
    public ResponseEntity<Map<String, String>> generarUrlDescarga(
        @PathVariable String id,
        @RequestParam(defaultValue = "60") int duracionMinutos) {
        try {
            String url = documentoService.obtenerUrlDescarga(id, duracionMinutos);
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("duracionMinutos", String.valueOf(duracionMinutos));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error generando URL de descarga: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/documentos/download-local/**
     * Descarga directa del archivo en modo Local Fallback
     */
    @GetMapping("/download-local/**")
    public ResponseEntity<byte[]> downloadLocalFile(HttpServletRequest request) {
        try {
            String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            AntPathMatcher apm = new AntPathMatcher();
            String s3Key = apm.extractPathWithinPattern(bestMatchPattern, path);
            
            logger.info("⬇️ Descargando archivo localmente (fallback): {}", s3Key);
            byte[] fileBytes = s3Service.downloadFile(s3Key);
            
            String filename = s3Key.contains("/") ? s3Key.substring(s3Key.lastIndexOf("/") + 1) : s3Key;
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileBytes);
        } catch (Exception e) {
            logger.error("❌ Error en descarga de archivo local: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}