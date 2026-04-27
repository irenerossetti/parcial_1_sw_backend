package com.workflow.backend.services;

import com.workflow.backend.models.Notificacion;
import com.workflow.backend.models.Usuario;
import com.workflow.backend.repositories.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public List<Notificacion> obtenerTodasParaUsuario(Usuario usuario) {
        List<String> ids = obtenerIdentificadoresUsuario(usuario);
        return notificacionRepository.findByUsuarioIdInOrderByCreadoEnDesc(ids);
    }

    public List<Notificacion> obtenerNoLeidasParaUsuario(Usuario usuario) {
        return obtenerTodasParaUsuario(usuario).stream()
                .filter(n -> !n.isLeida())
                .toList();
    }

    public long contarNoLeidas(Usuario usuario) {
        List<String> ids = obtenerIdentificadoresUsuario(usuario);
        return notificacionRepository.countByUsuarioIdInAndLeidaFalse(ids);
    }

    public Notificacion marcarComoLeida(String notificacionId, Usuario usuario) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificacion no encontrada"));

        if (!perteneceAlUsuario(notificacion, usuario)) {
            throw new RuntimeException("No autorizado para modificar esta notificacion");
        }

        if (!notificacion.isLeida()) {
            notificacion.setLeida(true);
            notificacion = notificacionRepository.save(notificacion);
        }

        return notificacion;
    }

    public void marcarTodasComoLeidas(Usuario usuario) {
        List<Notificacion> notificaciones = obtenerNoLeidasParaUsuario(usuario);
        if (notificaciones.isEmpty()) {
            return;
        }

        for (Notificacion notificacion : notificaciones) {
            notificacion.setLeida(true);
        }
        notificacionRepository.saveAll(notificaciones);
    }

    private boolean perteneceAlUsuario(Notificacion notificacion, Usuario usuario) {
        return obtenerIdentificadoresUsuario(usuario).contains(notificacion.getUsuarioId());
    }

    private List<String> obtenerIdentificadoresUsuario(Usuario usuario) {
        Set<String> ids = new LinkedHashSet<>();
        if (usuario.getId() != null && !usuario.getId().isBlank()) {
            ids.add(usuario.getId());
        }
        if (usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
            ids.add(usuario.getEmail());
        }
        return ids.stream().toList();
    }
}
