package com.workflow.backend.controllers;

import com.workflow.backend.models.Departamento;
import com.workflow.backend.services.DepartamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {

    @Autowired
    private DepartamentoService departamentoService;

    // Obtener todos los departamentos
    @GetMapping
    public ResponseEntity<List<Departamento>> getAllDepartamentos() {
        return ResponseEntity.ok(departamentoService.obtenerTodos());
    }

    // Obtener departamentos activos
    @GetMapping("/activos")
    public ResponseEntity<List<Departamento>> getDepartamentosActivos() {
        return ResponseEntity.ok(departamentoService.obtenerActivos());
    }

    // Obtener departamento por ID
    @GetMapping("/{id}")
    public ResponseEntity<Departamento> getDepartamentoById(@PathVariable String id) {
        Optional<Departamento> departamento = departamentoService.obtenerPorId(id);
        return departamento.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Obtener departamento por código
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Departamento> getDepartamentoByCodigo(@PathVariable String codigo) {
        Optional<Departamento> departamento = departamentoService.obtenerPorCodigo(codigo);
        return departamento.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear nuevo departamento (solo ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Departamento> createDepartamento(@RequestBody Departamento departamento) {
        return ResponseEntity.status(201).body(departamentoService.crear(departamento));
    }

    // Actualizar departamento (solo ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Departamento> updateDepartamento(@PathVariable String id, @RequestBody Departamento departamentoDetails) {
        Optional<Departamento> departamentoOpt = departamentoService.obtenerPorId(id);
        if (departamentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(departamentoService.actualizar(id, departamentoDetails));
    }

    // Actualizar métricas de departamento
    @PutMapping("/{id}/metricas")
    public ResponseEntity<Departamento> updateMetricas(@PathVariable String id, @RequestBody Departamento.MetricasDesempenio metricas) {
        Optional<Departamento> departamentoOpt = departamentoService.obtenerPorId(id);
        if (departamentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(departamentoService.actualizarMetricas(id, metricas));
    }

    // Eliminar departamento (solo ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartamento(@PathVariable String id) {
        departamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Obtener departamentos por tipo
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Departamento>> getDepartamentosByTipo(@PathVariable String tipo) {
        try {
            Departamento.TipoDepartamento tipoEnum = Departamento.TipoDepartamento.valueOf(tipo);
            return ResponseEntity.ok(departamentoService.obtenerPorTipo(tipoEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener siguiente departamento sugerido según el flujo
    @GetMapping("/{id}/siguientes")
    public ResponseEntity<List<String>> getSiguientesDepartamentos(@PathVariable String id) {
        Optional<Departamento> departamentoOpt = departamentoService.obtenerPorId(id);
        if (departamentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Departamento departamento = departamentoOpt.get();
        List<String> siguientes = departamento.getConfiguracionFlujo().getDepartamentosSiguientes();
        return ResponseEntity.ok(siguientes != null ? siguientes : List.of());
    }
}