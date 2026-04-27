package com.workflow.backend.controllers;

import com.workflow.backend.models.Tramite;
import com.workflow.backend.models.Usuario;
import com.workflow.backend.repositories.UsuarioRepository;
import com.workflow.backend.services.TramiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/tramites")
@CrossOrigin(origins = "http://localhost:4200")
public class TramiteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TramiteService tramiteService;

    // ==================== MÉTODOS BASE ====================

    @GetMapping
    public ResponseEntity<List<Tramite>> getAllTramites() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(tramiteService.obtenerTramitesPorUsuario(usuarioOpt.get()));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Tramite>> getTramitesByCliente(@PathVariable String clienteId) {
        return ResponseEntity.ok(tramiteService.obtenerPorCliente(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tramite> getTramiteById(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        Optional<Tramite> tramiteOpt = tramiteService.obtenerPorId(id);
        if (tramiteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();
        Tramite tramite = tramiteOpt.get();

        if (!puedeVerTramite(usuario, tramite)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(tramite);
    }

    @GetMapping("/{id}/ejecucion")
    public ResponseEntity<?> getEstadoEjecucion(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        Optional<Tramite> tramiteOpt = tramiteService.obtenerPorId(id);
        if (tramiteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();
        Tramite tramite = tramiteOpt.get();

        boolean puedeVer = puedeVerTramite(usuario, tramite);

        if (!puedeVer) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "No autorizado para ver este trámite");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        return ResponseEntity.ok(tramiteService.obtenerEstadoEjecucion(id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> descargarPdfTramite(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        Optional<Tramite> tramiteOpt = tramiteService.obtenerPorId(id);
        if (tramiteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();
        Tramite tramite = tramiteOpt.get();

        boolean puedeVer = puedeVerTramite(usuario, tramite);

        if (!puedeVer) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "No autorizado para descargar el PDF de este trámite");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        try {
            byte[] pdf = tramiteService.generarPdfCierre(id);
            String fileName = (tramite.getCodigo() != null && !tramite.getCodigo().isBlank())
                    ? "tramite-" + tramite.getCodigo() + ".pdf"
                    : "tramite-" + id + ".pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(pdf);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Tramite>> getTramitesByEstado(@PathVariable String estado) {
        try {
            Tramite.EstadoTramite estadoEnum = Tramite.EstadoTramite.valueOf(estado.toUpperCase());
            return ResponseEntity.ok(tramiteService.obtenerPorEstado(estadoEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== CREAR TRÁMITE ====================

    @PostMapping
    public ResponseEntity<Tramite> crearTramite(@RequestBody Tramite tramite) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        Tramite nuevoTramite = tramiteService.crearTramite(tramite, usuarioOpt.get());
        return new ResponseEntity<>(nuevoTramite, HttpStatus.CREATED);
    }

    // ==================== ACTUALIZAR TRÁMITE COMPLETO ====================

    @PutMapping("/{id}/avanzar")
    public ResponseEntity<?> avanzarTramite(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> body) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(401).build();
            }

            Usuario usuario = usuarioOpt.get();
            if (!"ADMIN".equals(usuario.getRol()) && !"FUNCIONARIO".equals(usuario.getRol())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("mensaje", "Solo ADMIN o FUNCIONARIO pueden avanzar trámites"));
            }

            Optional<Tramite> tramiteOpt = tramiteService.obtenerPorId(id);
            if (tramiteOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!puedeGestionarTramite(usuario, tramiteOpt.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("mensaje", "No puede gestionar trámites de otro departamento"));
            }

            String comentario = body != null && body.get("comentario") != null
                    ? String.valueOf(body.get("comentario"))
                    : "Avance registrado";

                String siguienteNodoId = body != null && body.get("siguienteNodoId") != null
                    ? String.valueOf(body.get("siguienteNodoId"))
                    : null;

                String departamentoDestino = body != null && body.get("departamentoDestino") != null
                    ? String.valueOf(body.get("departamentoDestino"))
                    : null;

            Map<String, Object> datosFormulario = new HashMap<>();
            if (body != null && body.get("datos") instanceof Map<?, ?> datosRaw) {
                for (Map.Entry<?, ?> entry : datosRaw.entrySet()) {
                    if (entry.getKey() != null) {
                        datosFormulario.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                }
            }

            Tramite actualizado = tramiteService.avanzarTramite(
                    id,
                    usuario.getEmail(),
                    comentario,
                    datosFormulario,
                    siguienteNodoId,
                    departamentoDestino
            );
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarTramite(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(401).build();
            }

            Usuario usuario = usuarioOpt.get();
            if (!"ADMIN".equals(usuario.getRol()) && !"FUNCIONARIO".equals(usuario.getRol())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("mensaje", "Solo ADMIN o FUNCIONARIO pueden rechazar trámites"));
            }

            Optional<Tramite> tramiteOpt = tramiteService.obtenerPorId(id);
            if (tramiteOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!puedeGestionarTramite(usuario, tramiteOpt.get())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("mensaje", "No puede gestionar trámites de otro departamento"));
            }

            String motivo = body != null && body.get("motivo") != null && !body.get("motivo").isBlank()
                    ? body.get("motivo")
                    : "Rechazado por funcionario";

            Tramite actualizado = tramiteService.rechazarTramite(id, usuario.getEmail(), motivo);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}/estado-completo")
    public ResponseEntity<?> actualizarEstadoCompleto(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(401).build();
            }

            Tramite actualizado = tramiteService.actualizarEstadoCompleto(id, body, usuarioOpt.get());
            return ResponseEntity.ok(actualizado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Tramite> actualizarEstado(@PathVariable String id, @RequestBody Map<String, String> body) {
        Optional<Tramite> tramiteOpt = tramiteService.obtenerPorId(id);
        if (tramiteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Tramite tramite = tramiteOpt.get();
        String nuevoEstado = body.get("estado");
        tramite.setEstado(Tramite.EstadoTramite.valueOf(nuevoEstado));
        tramite.setActualizadoEn(LocalDateTime.now());
        return ResponseEntity.ok(tramiteService.guardar(tramite));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTramite(@PathVariable String id) {
        if (tramiteService.existePorId(id)) {
            tramiteService.eliminarPorId(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private boolean puedeVerTramite(Usuario usuario, Tramite tramite) {
        if ("ADMIN".equals(usuario.getRol())) {
            return true;
        }

        if ("FUNCIONARIO".equals(usuario.getRol())) {
            return esMismoDepartamento(usuario, tramite);
        }

        return (tramite.getClienteEmail() != null && tramite.getClienteEmail().equals(usuario.getEmail()))
                || (tramite.getClienteId() != null && tramite.getClienteId().equals(usuario.getEmail()));
    }

    private boolean puedeGestionarTramite(Usuario usuario, Tramite tramite) {
        if ("ADMIN".equals(usuario.getRol())) {
            return true;
        }

        if ("FUNCIONARIO".equals(usuario.getRol())) {
            return esMismoDepartamento(usuario, tramite);
        }

        return false;
    }

    private boolean esMismoDepartamento(Usuario usuario, Tramite tramite) {
        String departamentoFuncionario = usuario.getDepartamentoNombre();
        String departamentoTramite = tramite.getDepartamentoActual();

        return departamentoFuncionario != null
                && !departamentoFuncionario.isBlank()
                && departamentoTramite != null
                && !departamentoTramite.isBlank()
                && departamentoFuncionario.equals(departamentoTramite);
    }

}