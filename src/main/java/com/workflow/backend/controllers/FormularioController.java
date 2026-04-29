package com.workflow.backend.controllers;

import com.workflow.backend.models.CampoFormulario;
import com.workflow.backend.models.PoliticaNegocio;
import com.workflow.backend.repositories.PoliticaNegocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/formularios")
@CrossOrigin(origins = "*")
public class FormularioController {

    @Autowired
    private PoliticaNegocioRepository politicaRepository;

    /**
     * Obtener formulario dinámico para un nodo específico de una política
     */
    @GetMapping("/nodo/{politicaId}/{nodoId}")
    public ResponseEntity<?> getFormularioNodo(
            @PathVariable String politicaId,
            @PathVariable String nodoId) {
        
        try {
            PoliticaNegocio politica = politicaRepository.findById(politicaId)
                    .orElseThrow(() -> new RuntimeException("Política no encontrada"));

            // Buscar el nodo específico
            PoliticaNegocio.Nodo nodo = politica.getFlujo().stream()
                    .filter(n -> n.getNodoId().equals(nodoId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Nodo no encontrado"));

            // Obtener los campos del formulario para este nodo
            List<CampoFormulario> camposNodo = new ArrayList<>();
            
            if (nodo.getCamposFormulario() != null && !nodo.getCamposFormulario().isEmpty()) {
                // Filtrar campos de la política que corresponden a este nodo
                for (String nombreCampo : nodo.getCamposFormulario()) {
                    politica.getCampos().stream()
                            .filter(c -> c.getNombre().equals(nombreCampo))
                            .findFirst()
                            .ifPresent(camposNodo::add);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("nodoId", nodo.getNodoId());
            response.put("nombreNodo", nodo.getNombre());
            response.put("descripcion", nodo.getDescripcion());
            response.put("campos", camposNodo);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Obtener todos los campos de formulario de una política
     */
    @GetMapping("/politica/{politicaId}")
    public ResponseEntity<?> getFormularioPolitica(@PathVariable String politicaId) {
        try {
            PoliticaNegocio politica = politicaRepository.findById(politicaId)
                    .orElseThrow(() -> new RuntimeException("Política no encontrada"));

            Map<String, Object> response = new HashMap<>();
            response.put("politicaId", politica.getId());
            response.put("nombre", politica.getNombre());
            response.put("campos", politica.getCampos() != null ? politica.getCampos() : new ArrayList<>());

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Actualizar campos de formulario de una política
     */
    @PutMapping("/politica/{politicaId}/campos")
    public ResponseEntity<?> actualizarCamposPolitica(
            @PathVariable String politicaId,
            @RequestBody List<CampoFormulario> campos) {
        
        try {
            PoliticaNegocio politica = politicaRepository.findById(politicaId)
                    .orElseThrow(() -> new RuntimeException("Política no encontrada"));

            politica.setCampos(campos);
            politicaRepository.save(politica);

            return ResponseEntity.ok(politica);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
