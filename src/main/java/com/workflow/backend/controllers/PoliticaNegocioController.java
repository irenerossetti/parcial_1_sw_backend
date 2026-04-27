package com.workflow.backend.controllers;

import com.workflow.backend.models.PoliticaNegocio;
import com.workflow.backend.services.PoliticaNegocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/politicas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PoliticaNegocioController {

    private final PoliticaNegocioService politicaService;

    @GetMapping
    public List<PoliticaNegocio> obtenerTodas() {
        return politicaService.obtenerTodas();
    }

    @PostMapping
    public ResponseEntity<PoliticaNegocio> crear(@RequestBody PoliticaNegocio politica) {
        return ResponseEntity.status(201).body(politicaService.crear(politica));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoliticaNegocio> obtenerPorId(@PathVariable String id) {
        return politicaService.obtenerPorIdOptional(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PoliticaNegocio> actualizar(@PathVariable String id, @RequestBody PoliticaNegocio politica) {
        return politicaService.obtenerPorIdOptional(id)
                .map(existente -> {
                    politica.setId(id);
                    return ResponseEntity.ok(politicaService.actualizar(politica));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (politicaService.obtenerPorIdOptional(id).isPresent()) {
            politicaService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}