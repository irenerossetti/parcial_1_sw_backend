package com.workflow.backend.repositories;

import com.workflow.backend.models.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    // Spring genera automáticamente estas consultas por el nombre del método
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}