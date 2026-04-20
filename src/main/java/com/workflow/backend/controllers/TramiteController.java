package com.workflow.backend.controllers;

import com.workflow.backend.models.Tramite;
import com.workflow.backend.services.TramiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tramites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TramiteController {

    private final TramiteService tramiteService;

    // POST /api/tramites/iniciar
    // Body: { "clienteId": "xxx", "politicaId": "yyy" }
    @PostMapping("/iniciar")
    public ResponseEntity<Tramite> iniciar(@RequestBody Map<String, String> body) {
        Tramite tramite = tramiteService.crearTramite(
                body.get("clienteId"),
                body.get("politicaId")
        );
        return ResponseEntity.ok(tramite);
    }

    // POST /api/tramites/{id}/avanzar
    @PostMapping("/{id}/avanzar")
    public ResponseEntity<Tramite> avanzar(@PathVariable String id,
                                           @RequestBody Map<String, Object> body) {
        Tramite tramite = tramiteService.avanzarTramite(
                id,
                (String) body.get("funcionarioId"),
                (String) body.get("comentario"),
                (Map<String, Object>) body.get("datos")
        );
        return ResponseEntity.ok(tramite);
    }

    // POST /api/tramites/{id}/rechazar
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Tramite> rechazar(@PathVariable String id,
                                            @RequestBody Map<String, String> body) {
        Tramite tramite = tramiteService.rechazarTramite(
                id,
                body.get("funcionarioId"),
                body.get("motivo")
        );
        return ResponseEntity.ok(tramite);
    }

    // GET /api/tramites/cliente/{clienteId}
    @GetMapping("/cliente/{clienteId}")
    public List<Tramite> porCliente(@PathVariable String clienteId) {
        return tramiteService.obtenerPorCliente(clienteId);
    }

    // GET /api/tramites/{id}/estado
    @GetMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> estado(@PathVariable String id) {
        return ResponseEntity.ok(tramiteService.verEstadoTramite(id));
    }

    // GET /api/tramites/estado/NUEVO
    @GetMapping("/estado/{estado}")
    public List<Tramite> porEstado(@PathVariable Tramite.EstadoTramite estado) {
        return tramiteService.obtenerPorEstado(estado);
    }
}