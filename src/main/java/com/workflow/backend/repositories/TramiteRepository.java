package com.workflow.backend.repositories;

import com.workflow.backend.models.Tramite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TramiteRepository extends MongoRepository<Tramite, String> {
    List<Tramite> findByClienteId(String clienteId);
    List<Tramite> findByNodoActualId(String nodoId);
    List<Tramite> findByEstado(Tramite.EstadoTramite estado);
}