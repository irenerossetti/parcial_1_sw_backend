package com.workflow.backend.services;

import com.workflow.backend.models.*;
import com.workflow.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TramiteService {

    private final TramiteRepository tramiteRepository;
    private final PoliticaNegocioRepository politicaRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    // ── Crear un trámite nuevo ───────────────────────────────────
    public Tramite crearTramite(String clienteId, String politicaId) {

        // 1. Buscar la política de negocio
        PoliticaNegocio politica = politicaRepository.findById(politicaId)
                .orElseThrow(() -> new RuntimeException("Política no encontrada"));

        // 2. Encontrar el nodo de INICIO
        PoliticaNegocio.Nodo nodoInicio = politica.getFlujo().stream()
                .filter(n -> n.getTipo() == PoliticaNegocio.Nodo.TipoNodo.INICIO)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("La política no tiene nodo de inicio"));

        // 3. El primer nodo real es el que sigue al INICIO
        String primerNodoId = nodoInicio.getSiguientes().get(0);
        PoliticaNegocio.Nodo primerNodo = buscarNodo(politica, primerNodoId);

        // 4. Crear el historial del primer paso
        Tramite.HistorialPaso primerPaso = new Tramite.HistorialPaso();
        primerPaso.setNodoId(primerNodo.getNodoId());
        primerPaso.setNombreNodo(primerNodo.getNombre());
        primerPaso.setDepartamentoId(primerNodo.getDepartamentoId());
        primerPaso.setEstado(Tramite.HistorialPaso.EstadoPaso.PENDIENTE);
        primerPaso.setIniciadoEn(LocalDateTime.now());

        // 5. Construir el trámite
        Tramite tramite = new Tramite();
        tramite.setCodigo(generarCodigo());
        tramite.setClienteId(clienteId);
        tramite.setPoliticaId(politicaId);
        tramite.setEstado(Tramite.EstadoTramite.NUEVO);
        tramite.setNodoActualId(primerNodoId);
        tramite.setHistorial(new ArrayList<>(List.of(primerPaso)));
        tramite.setDatosFormulario(new HashMap<>());
        tramite.setCreadoEn(LocalDateTime.now());
        tramite.setActualizadoEn(LocalDateTime.now());

        Tramite guardado = tramiteRepository.save(tramite);

        // 6. Notificar al departamento responsable
        notificarDepartamento(guardado, primerNodo, politica.getNombre());

        return guardado;
    }

    // ── Avanzar el trámite al siguiente paso ─────────────────────
    public Tramite avanzarTramite(String tramiteId, String funcionarioId,
                                  String comentario, Map<String, Object> datos) {

        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));

        PoliticaNegocio politica = politicaRepository.findById(tramite.getPoliticaId())
                .orElseThrow(() -> new RuntimeException("Política no encontrada"));

        // 1. Completar el paso actual en el historial
        Tramite.HistorialPaso pasoActual = tramite.getHistorial().stream()
                .filter(p -> p.getNodoId().equals(tramite.getNodoActualId())
                        && p.getEstado() == Tramite.HistorialPaso.EstadoPaso.PENDIENTE)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No hay paso pendiente"));

        pasoActual.setFuncionarioId(funcionarioId);
        pasoActual.setComentario(comentario);
        pasoActual.setDatosIngresados(datos);
        pasoActual.setEstado(Tramite.HistorialPaso.EstadoPaso.COMPLETADO);
        pasoActual.setCompletadoEn(LocalDateTime.now());

        // Guardar datos del formulario en el trámite
        if (datos != null) tramite.getDatosFormulario().putAll(datos);

        // 2. Buscar el nodo actual y sus siguientes
        PoliticaNegocio.Nodo nodoActual = buscarNodo(politica, tramite.getNodoActualId());

        // 3. ¿Hay siguiente nodo?
        if (nodoActual.getSiguientes() == null || nodoActual.getSiguientes().isEmpty()
                || nodoActual.getTipo() == PoliticaNegocio.Nodo.TipoNodo.FIN) {

            // El trámite terminó
            tramite.setEstado(Tramite.EstadoTramite.COMPLETADO);
            tramite.setFinalizadoEn(LocalDateTime.now());

            // Notificar al cliente
            notificarCliente(tramite, "¡Tu trámite ha sido completado!",
                    "Tu trámite " + tramite.getCodigo() + " ha finalizado exitosamente.",
                    Notificacion.TipoNotificacion.TRAMITE_COMPLETADO);

        } else {
            // Avanzar al siguiente nodo
            String siguienteNodoId = nodoActual.getSiguientes().get(0);
            PoliticaNegocio.Nodo siguienteNodo = buscarNodo(politica, siguienteNodoId);

            // Crear nuevo paso en el historial
            Tramite.HistorialPaso nuevoPaso = new Tramite.HistorialPaso();
            nuevoPaso.setNodoId(siguienteNodo.getNodoId());
            nuevoPaso.setNombreNodo(siguienteNodo.getNombre());
            nuevoPaso.setDepartamentoId(siguienteNodo.getDepartamentoId());
            nuevoPaso.setEstado(Tramite.HistorialPaso.EstadoPaso.PENDIENTE);
            nuevoPaso.setIniciadoEn(LocalDateTime.now());

            tramite.getHistorial().add(nuevoPaso);
            tramite.setNodoActualId(siguienteNodoId);
            tramite.setEstado(Tramite.EstadoTramite.EN_PROCESO);

            // Notificar al siguiente departamento
            notificarDepartamento(tramite, siguienteNodo, "Trámite avanzado");

            // Notificar al cliente que avanzó
            notificarCliente(tramite, "Tu trámite avanzó",
                    "Tu trámite " + tramite.getCodigo() + " pasó a: " + siguienteNodo.getNombre(),
                    Notificacion.TipoNotificacion.TRAMITE_AVANZADO);
        }

        tramite.setActualizadoEn(LocalDateTime.now());
        return tramiteRepository.save(tramite);
    }

    // ── Rechazar un trámite ──────────────────────────────────────
    public Tramite rechazarTramite(String tramiteId, String funcionarioId, String motivo) {

        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));

        // Marcar paso actual como rechazado
        tramite.getHistorial().stream()
                .filter(p -> p.getNodoId().equals(tramite.getNodoActualId())
                        && p.getEstado() == Tramite.HistorialPaso.EstadoPaso.PENDIENTE)
                .findFirst()
                .ifPresent(p -> {
                    p.setEstado(Tramite.HistorialPaso.EstadoPaso.RECHAZADO);
                    p.setFuncionarioId(funcionarioId);
                    p.setComentario(motivo);
                    p.setCompletadoEn(LocalDateTime.now());
                });

        tramite.setEstado(Tramite.EstadoTramite.RECHAZADO);
        tramite.setActualizadoEn(LocalDateTime.now());

        // Notificar al cliente
        notificarCliente(tramite, "Tu trámite fue rechazado",
                "Tu trámite " + tramite.getCodigo() + " fue rechazado. Motivo: " + motivo,
                Notificacion.TipoNotificacion.TRAMITE_RECHAZADO);

        return tramiteRepository.save(tramite);
    }

    // ── Obtener trámites por cliente ─────────────────────────────
    public List<Tramite> obtenerPorCliente(String clienteId) {
        return tramiteRepository.findByClienteId(clienteId);
    }

    // ── Obtener trámites por estado ──────────────────────────────
    public List<Tramite> obtenerPorEstado(Tramite.EstadoTramite estado) {
        return tramiteRepository.findByEstado(estado);
    }

    // ── Ver estado actual del trámite (para el cliente) ──────────
    public Map<String, Object> verEstadoTramite(String tramiteId) {

        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));

        PoliticaNegocio politica = politicaRepository.findById(tramite.getPoliticaId())
                .orElseThrow(() -> new RuntimeException("Política no encontrada"));

        // Calcular progreso
        int totalNodos = (int) politica.getFlujo().stream()
                .filter(n -> n.getTipo() != PoliticaNegocio.Nodo.TipoNodo.INICIO
                        && n.getTipo() != PoliticaNegocio.Nodo.TipoNodo.FIN)
                .count();

        int nodosCompletados = (int) tramite.getHistorial().stream()
                .filter(p -> p.getEstado() == Tramite.HistorialPaso.EstadoPaso.COMPLETADO)
                .count();

        int porcentaje = totalNodos > 0 ? (nodosCompletados * 100) / totalNodos : 0;

        Map<String, Object> estado = new HashMap<>();
        estado.put("codigo", tramite.getCodigo());
        estado.put("estado", tramite.getEstado());
        estado.put("porcentajeAvance", porcentaje);
        estado.put("nodosCompletados", nodosCompletados);
        estado.put("totalNodos", totalNodos);
        estado.put("nodoActual", buscarNodo(politica, tramite.getNodoActualId()));
        estado.put("historial", tramite.getHistorial());
        estado.put("creadoEn", tramite.getCreadoEn());

        return estado;
    }

    // ── Métodos auxiliares ───────────────────────────────────────

    private PoliticaNegocio.Nodo buscarNodo(PoliticaNegocio politica, String nodoId) {
        return politica.getFlujo().stream()
                .filter(n -> n.getNodoId().equals(nodoId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado: " + nodoId));
    }

    private void notificarDepartamento(Tramite tramite, PoliticaNegocio.Nodo nodo, String nombrePolitica) {
        // Notificar al responsable del nodo si existe
        if (nodo.getResponsableId() != null) {
            Notificacion notif = new Notificacion();
            notif.setUsuarioId(nodo.getResponsableId());
            notif.setTramiteId(tramite.getId());
            notif.setTramiteCodigo(tramite.getCodigo());
            notif.setTitulo("Nuevo trámite asignado");
            notif.setMensaje("Tienes un nuevo trámite en: " + nodo.getNombre());
            notif.setTipo(Notificacion.TipoNotificacion.NUEVO_TRAMITE);
            notificacionRepository.save(notif);
        }
    }

    private void notificarCliente(Tramite tramite, String titulo,
                                  String mensaje, Notificacion.TipoNotificacion tipo) {
        Notificacion notif = new Notificacion();
        notif.setUsuarioId(tramite.getClienteId());
        notif.setTramiteId(tramite.getId());
        notif.setTramiteCodigo(tramite.getCodigo());
        notif.setTitulo(titulo);
        notif.setMensaje(mensaje);
        notif.setTipo(tipo);
        notificacionRepository.save(notif);
    }

    private String generarCodigo() {
        int anio = LocalDateTime.now().getYear();
        long count = tramiteRepository.count() + 1;
        return String.format("TRM-%d-%04d", anio, count);
    }
}