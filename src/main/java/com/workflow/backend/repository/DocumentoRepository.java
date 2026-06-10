package com.workflow.backend.repository;

import com.workflow.backend.model.Documento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DAO para Documento
 * Acceso a datos en MongoDB
 */
@Repository
public interface DocumentoRepository extends MongoRepository<Documento, String> {
    
    /**
     * Buscar todos los documentos de un trámite
     * @param tramiteId ID del trámite
     * @return Lista de documentos
     */
    List<Documento> findByTramiteId(String tramiteId);
    
    /**
     * Buscar documentos creados por un usuario
     * @param creadoPor ID del usuario
     * @return Lista de documentos
     */
    List<Documento> findByCreadoPor(String creadoPor);
    
    /**
     * Buscar documentos por estado
     * @param estado Estado (PENDIENTE, REVISIÓN, APROBADO, RECHAZADO)
     * @return Lista de documentos
     */
    List<Documento> findByEstado(String estado);
    
    /**
     * Buscar documentos por s3Key
     * @param s3Key Ruta en S3
     * @return Documento si existe
     */
    Optional<Documento> findByS3Key(String s3Key);
    
    /**
     * Query personalizada: buscar documentos por tramiteId y estado
     * @param tramiteId ID del trámite
     * @param estado Estado del documento
     * @return Lista de documentos
     */
    @Query("{ 'tramiteId': ?0, 'estado': ?1 }")
    List<Documento> findByTramiteIdAndEstado(String tramiteId, String estado);
    
    /**
     * Eliminar todos los documentos de un trámite
     * @param tramiteId ID del trámite
     */
    void deleteByTramiteId(String tramiteId);
}
