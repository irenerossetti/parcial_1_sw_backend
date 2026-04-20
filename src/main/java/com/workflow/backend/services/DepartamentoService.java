package com.workflow.backend.services;

import com.workflow.backend.models.Departamento;
import com.workflow.backend.repositories.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public List<Departamento> obtenerTodos() {
        return departamentoRepository.findAll();
    }

    public Departamento crear(Departamento departamento) {
        return departamentoRepository.save(departamento);
    }

    public Departamento obtenerPorId(String id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));
    }
}