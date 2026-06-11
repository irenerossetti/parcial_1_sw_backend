package com.workflow.backend.controllers;

import com.workflow.backend.models.Notificacion;
import com.workflow.backend.models.Usuario;
import com.workflow.backend.repositories.UsuarioRepository;
import com.workflow.backend.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<?> obtenerTodas() {
        Optional<Usuario> usuarioOpt = getUsuarioAutenticado();
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Notificacion> notificaciones = notificacionService.obtenerTodasParaUsuario(usuarioOpt.get());
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<?> obtenerNoLeidas() {
        Optional<Usuario> usuarioOpt = getUsuarioAutenticado();
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Notificacion> notificaciones = notificacionService.obtenerNoLeidasParaUsuario(usuarioOpt.get());
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/resumen")
    public ResponseEntity<?> resumen() {
        Optional<Usuario> usuarioOpt = getUsuarioAutenticado();
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        long noLeidas = notificacionService.contarNoLeidas(usuarioOpt.get());
        Map<String, Object> payload = new HashMap<>();
        payload.put("noLeidas", noLeidas);
        return ResponseEntity.ok(payload);
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<?> marcarComoLeida(@PathVariable String id) {
        Optional<Usuario> usuarioOpt = getUsuarioAutenticado();
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Notificacion actualizada = notificacionService.marcarComoLeida(id, usuarioOpt.get());
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/leer-todas")
    public ResponseEntity<?> marcarTodasComoLeidas() {
        Optional<Usuario> usuarioOpt = getUsuarioAutenticado();
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        notificacionService.marcarTodasComoLeidas(usuarioOpt.get());
        return ResponseEntity.ok(Map.of("mensaje", "Notificaciones marcadas como leidas"));
    }

    private Optional<Usuario> getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Optional.empty();
        }
        String email = auth.getName();
        return usuarioRepository.findByEmail(email);
    }
}
