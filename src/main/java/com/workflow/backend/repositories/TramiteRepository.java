package com.workflow.backend.repositories;

import com.workflow.backend.models.Tramite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TramiteRepository extends MongoRepository<Tramite, String> {

    List<Tramite> findByDepartamentoActual(String departamentoActual);

    List<Tramite> findByClienteEmail(String clienteEmail);
    // Buscar trámites por cliente
    List<Tramite> findByClienteId(String clienteId);

    // Buscar trámites por estado
    List<Tramite> findByEstado(Tramite.EstadoTramite estado);

    // Buscar trámites por política
    List<Tramite> findByPoliticaId(String politicaId);

    // Buscar trámite por código
    List<Tramite> findByCodigo(String codigo);
}