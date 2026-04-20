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
@CrossOrigin(origins = "*")
public class PoliticaNegocioController {

    private final PoliticaNegocioService politicaService;

    @GetMapping
    public List<PoliticaNegocio> obtenerTodas() {
        return politicaService.obtenerTodas();
    }

    @PostMapping
    public ResponseEntity<PoliticaNegocio> crear(@RequestBody PoliticaNegocio politica) {
        return ResponseEntity.ok(politicaService.crear(politica));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoliticaNegocio> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(politicaService.obtenerPorId(id));
    }
}