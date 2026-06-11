package com.workflow.backend.controllers;

import com.workflow.backend.models.CampoFormulario;
import com.workflow.backend.models.PoliticaNegocio;
import com.workflow.backend.repositories.PoliticaNegocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/politicas")
public class PoliticaFormularioController {

    @Autowired
    private PoliticaNegocioRepository politicaNegocioRepository;

    /**
     * Obtiene los campos dinámicos de una política
     */
    @GetMapping("/{politicaId}/campos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> obtenerCamposPorPolitica(@PathVariable String politicaId) {
        Optional<PoliticaNegocio> politicaOpt = politicaNegocioRepository.findById(politicaId);
        
        if (!politicaOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        PoliticaNegocio politica = politicaOpt.get();
        List<CampoFormulario> campos = politica.getCampos();
        
        if (campos == null || campos.isEmpty()) {
            // Si no hay campos, retornar lista vacía
            return ResponseEntity.ok(List.of());
        }

        // Ordenar por el campo 'orden'
        campos.sort((a, b) -> Integer.compare(a.getOrden(), b.getOrden()));
        
        return ResponseEntity.ok(campos);
    }

    /**
     * Obtiene la configuración completa del formulario de una política
     */
    @GetMapping("/{politicaId}/formulario")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> obtenerConfiguracionFormulario(@PathVariable String politicaId) {
        Optional<PoliticaNegocio> politicaOpt = politicaNegocioRepository.findById(politicaId);
        
        if (!politicaOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        PoliticaNegocio politica = politicaOpt.get();
        
        var configuracion = new java.util.HashMap<>();
        configuracion.put("politicaId", politica.getId());
        configuracion.put("politicaNombre", politica.getNombre());
        
        List<CampoFormulario> campos = politica.getCampos();
        if (campos != null) {
            campos.sort((a, b) -> Integer.compare(a.getOrden(), b.getOrden()));
        }
        configuracion.put("campos", campos != null ? campos : List.of());
        
        return ResponseEntity.ok(configuracion);
    }

    /**
     * Valida los datos enviados contra los campos de la política
     */
    @PostMapping("/{politicaId}/validar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> validarDatos(
        @PathVariable String politicaId,
        @RequestBody java.util.Map<String, Object> datos) {
        
        Optional<PoliticaNegocio> politicaOpt = politicaNegocioRepository.findById(politicaId);
        
        if (!politicaOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        PoliticaNegocio politica = politicaOpt.get();
        List<CampoFormulario> campos = politica.getCampos();
        
        var errores = new java.util.HashMap<String, String>();
        
        if (campos != null) {
            for (CampoFormulario campo : campos) {
                Object valor = datos.get(campo.getNombre());
                
                // Validar campos requeridos
                if (campo.isRequerido() && (valor == null || valor.toString().trim().isEmpty())) {
                    errores.put(campo.getNombre(), campo.getEtiqueta() + " es requerido");
                    continue;
                }
                
                // Validar formato con regex
                if (campo.getValidacion() != null && valor != null && !valor.toString().isEmpty()) {
                    if (!valor.toString().matches(campo.getValidacion())) {
                        errores.put(campo.getNombre(), campo.getEtiqueta() + " tiene formato inválido");
                    }
                }
            }
        }
        
        var respuesta = new java.util.HashMap<>();
        respuesta.put("valido", errores.isEmpty());
        if (!errores.isEmpty()) {
            respuesta.put("errores", errores);
        }
        
        return ResponseEntity.ok(respuesta);
    }
}
