package com.workflow.backend.repositories;

import com.workflow.backend.models.Departamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartamentoRepository extends MongoRepository<Departamento, String> {
    Optional<Departamento> findByCodigo(String codigo);
    List<Departamento> findByEstado(Departamento.EstadoDepartamento estado);
    List<Departamento> findByTipo(Departamento.TipoDepartamento tipo);
    List<Departamento> findByResponsableEmail(String responsableEmail);
    List<Departamento> findByEstadoAndTipo(Departamento.EstadoDepartamento estado, Departamento.TipoDepartamento tipo);
}