package com.workflow.backend.services;

import com.workflow.backend.models.Departamento;
import com.workflow.backend.repositories.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public List<Departamento> obtenerTodos() {
        return departamentoRepository.findAll();
    }

    public List<Departamento> obtenerActivos() {
        List<Departamento> departamentos = departamentoRepository.findAll();
        return departamentos.stream()
                .filter(d -> d.getEstado() == null || d.getEstado() == Departamento.EstadoDepartamento.ACTIVO)
                .toList();
    }

    public List<Departamento> obtenerPorTipo(Departamento.TipoDepartamento tipo) {
        return departamentoRepository.findByTipo(tipo);
    }

    public Optional<Departamento> obtenerPorCodigo(String codigo) {
        return departamentoRepository.findByCodigo(codigo);
    }

    public Optional<Departamento> obtenerPorId(String id) {
        return departamentoRepository.findById(id);
    }

    public Departamento crear(Departamento departamento) {
        if (departamento.getCodigo() == null || departamento.getCodigo().isBlank()) {
            departamento.setCodigo("DPT-" + System.currentTimeMillis());
        }

        if (departamento.getEstado() == null) {
            departamento.setEstado(Departamento.EstadoDepartamento.ACTIVO);
        }

        if (departamento.getMetricas() == null) {
            departamento.setMetricas(new Departamento.MetricasDesempenio());
        }

        if (departamento.getConfiguracionFlujo() == null) {
            departamento.setConfiguracionFlujo(new Departamento.ConfiguracionFlujo());
        }

        departamento.setActualizadoEn(LocalDateTime.now());
        return departamentoRepository.save(departamento);
    }

    public Departamento actualizar(String id, Departamento departamentoDetails) {
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));

        departamento.setNombre(departamentoDetails.getNombre());
        departamento.setDescripcion(departamentoDetails.getDescripcion());
        departamento.setResponsableEmail(departamentoDetails.getResponsableEmail());
        departamento.setResponsableNombre(departamentoDetails.getResponsableNombre());
        departamento.setTipo(departamentoDetails.getTipo());
        departamento.setEstado(departamentoDetails.getEstado());
        departamento.setRolesPermitidos(departamentoDetails.getRolesPermitidos());
        departamento.setConfiguracionFlujo(departamentoDetails.getConfiguracionFlujo());
        departamento.setActualizadoEn(LocalDateTime.now());

        return departamentoRepository.save(departamento);
    }

    public Departamento actualizarMetricas(String id, Departamento.MetricasDesempenio metricas) {
        Departamento departamento = departamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));

        metricas.setUltimaActualizacion(LocalDateTime.now());
        departamento.setMetricas(metricas);
        departamento.setActualizadoEn(LocalDateTime.now());

        return departamentoRepository.save(departamento);
    }

    public void eliminar(String id) {
        departamentoRepository.deleteById(id);
    }
}