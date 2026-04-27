package com.workflow.backend.repositories;

import com.workflow.backend.models.Notificacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends MongoRepository<Notificacion, String> {
    List<Notificacion> findByUsuarioIdAndLeidaFalse(String usuarioId);
    List<Notificacion> findByUsuarioId(String usuarioId);
    List<Notificacion> findByUsuarioIdOrderByCreadoEnDesc(String usuarioId);
    List<Notificacion> findByUsuarioIdInOrderByCreadoEnDesc(List<String> usuarioIds);
    long countByUsuarioIdInAndLeidaFalse(List<String> usuarioIds);
    List<Notificacion> findByTramiteIdOrderByCreadoEnAsc(String tramiteId);
}