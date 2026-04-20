package com.workflow.backend.repositories;

import com.workflow.backend.models.Departamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartamentoRepository extends MongoRepository<Departamento, String> {
    boolean existsByNombre(String nombre);
}