package com.workflow.backend.services;

import com.workflow.backend.models.*;
import com.workflow.backend.repositories.*;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TramiteService {

    private final TramiteRepository tramiteRepository;
    private final PoliticaNegocioRepository politicaRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    // ── Crear un trámite nuevo ───────────────────────────────────
    public Tramite crearTramite(String clienteId, String politicaId) {
        Tramite tramite = new Tramite();
        tramite.setCodigo(generarCodigo());
        tramite.setClienteId(clienteId);
        tramite.setClienteEmail(clienteId);
        tramite.setPoliticaId(politicaId);
        inicializarDesdePoliticaSiAplica(tramite);
        tramite.setDatosFormulario(new HashMap<>());
        tramite.setCreadoEn(LocalDateTime.now());
        tramite.setActualizadoEn(LocalDateTime.now());

        return tramiteRepository.save(tramite);
    }

        // ── Crear trámite desde un usuario autenticado ───────────────
        public Tramite crearTramite(Tramite tramiteSolicitado, Usuario usuario) {
                if (usuario == null || usuario.getEmail() == null) {
                        throw new RuntimeException("Usuario no autenticado");
                }

                Tramite tramite = tramiteSolicitado != null ? tramiteSolicitado : new Tramite();
                tramite.setCreadoEn(LocalDateTime.now());
                tramite.setActualizadoEn(LocalDateTime.now());

                if (tramite.getEstado() == null) {
                        tramite.setEstado(Tramite.EstadoTramite.NUEVO);
                }

                tramite.setClienteEmail(usuario.getEmail());
                tramite.setClienteId(usuario.getEmail());
                tramite.setClienteNombre(usuario.getNombre());

                inicializarDesdePoliticaSiAplica(tramite);

                if (tramite.getEstado() == null) {
                        tramite.setEstado(Tramite.EstadoTramite.NUEVO);
                }

                if ((tramite.getDepartamentoActual() == null || tramite.getDepartamentoActual().isBlank())
                                && tramite.getNodoActualId() == null) {
                        tramite.setDepartamentoActual("Atención al Cliente");
                }

                if (tramite.getHistorial() == null) {
                        tramite.setHistorial(new ArrayList<>());
                }

                if (tramite.getDatosFormulario() == null) {
                        tramite.setDatosFormulario(new HashMap<>());
                }

                if (tramite.getCodigo() == null || tramite.getCodigo().isBlank()) {
                        tramite.setCodigo(generarCodigo());
                }

                Tramite guardado = tramiteRepository.save(tramite);

                // Notificar por WebSocket a todos los clientes
                webSocketNotificationService.notificarNuevoTramite(guardado);

                if (guardado.getPoliticaId() != null && guardado.getNodoActualId() != null) {
                        PoliticaNegocio politica = politicaRepository.findById(guardado.getPoliticaId())
                                        .orElse(null);
                        if (politica != null) {
                                PoliticaNegocio.Nodo nodoActual = buscarNodo(politica, guardado.getNodoActualId());
                                notificarDepartamento(guardado, nodoActual, politica.getNombre());
                        }
                }

                return guardado;
        }

        // ── Obtener trámites según el rol del usuario ────────────────
        public List<Tramite> obtenerTramitesPorUsuario(Usuario usuario) {
                if (usuario == null) {
                        return List.of();
                }

                if ("ADMIN".equals(usuario.getRol())) {
                        return tramiteRepository.findAll();
                }

                if ("FUNCIONARIO".equals(usuario.getRol())) {
                        String departamentoFuncionario = usuario.getDepartamentoNombre();
                        if (departamentoFuncionario == null || departamentoFuncionario.isBlank()) {
                                System.out.println("[WARN] Funcionario " + usuario.getEmail() + " sin departamento asignado");
                                return List.of();
                        }

                        return tramiteRepository.findAll().stream()
                                .filter(tramite -> tramite.getEstado() != Tramite.EstadoTramite.COMPLETADO 
                                        && tramite.getEstado() != Tramite.EstadoTramite.RECHAZADO)
                                .filter(tramite -> departamentoFuncionario.equals(tramite.getDepartamentoActual()))
                                .toList();
                }

                return tramiteRepository.findByClienteEmail(usuario.getEmail());
        }

        // ── Obtener trámites por cliente ─────────────────────────────
        public List<Tramite> obtenerPorCliente(String clienteId) {
                return tramiteRepository.findByClienteId(clienteId);
        }

        public Optional<Tramite> obtenerPorId(String tramiteId) {
                return tramiteRepository.findById(tramiteId);
        }

        public Tramite guardar(Tramite tramite) {
                return tramiteRepository.save(tramite);
        }

        public boolean existePorId(String tramiteId) {
                return tramiteRepository.existsById(tramiteId);
        }

        public void eliminarPorId(String tramiteId) {
                tramiteRepository.deleteById(tramiteId);
        }

        // ── Actualizar un trámite desde la pantalla operativa ────────
        public Tramite actualizarEstadoCompleto(String tramiteId, Map<String, String> body, Usuario usuario) {
                Tramite tramite = tramiteRepository.findById(tramiteId)
                                .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));

                if (usuario == null) {
                        throw new RuntimeException("Usuario no autenticado");
                }

                if ("FUNCIONARIO".equals(usuario.getRol())) {
                        String departamentoFuncionario = usuario.getDepartamentoNombre();
                        String departamentoTramite = tramite.getDepartamentoActual();

                        if (departamentoFuncionario != null && departamentoTramite != null
                                        && !departamentoFuncionario.equals(departamentoTramite)) {
                                throw new RuntimeException("No puede modificar trámites de otro departamento");
                        }
                }

                String nuevoEstadoStr = body.get("estado");
                String nuevoDepartamento = body.get("departamento");
                String observacion = body.get("observaciones");

                Tramite.EstadoTramite nuevoEstado = Tramite.EstadoTramite.valueOf(nuevoEstadoStr);

                Tramite.HistorialPaso paso = new Tramite.HistorialPaso();
                paso.setNodoId(tramite.getNodoActualId() != null ? tramite.getNodoActualId() : "INICIO");
                paso.setNombreNodo(obtenerNombreNodoPorEstado(nuevoEstado));
                paso.setDepartamentoId(nuevoDepartamento);
                paso.setFuncionarioId(usuario.getEmail());
                paso.setComentario(observacion);
                paso.setEstado(Tramite.HistorialPaso.EstadoPaso.COMPLETADO);
                paso.setIniciadoEn(LocalDateTime.now());
                paso.setCompletadoEn(LocalDateTime.now());

                if (tramite.getHistorial() == null) {
                        tramite.setHistorial(new ArrayList<>());
                }
                tramite.getHistorial().add(paso);

                tramite.setEstado(nuevoEstado);
                if (nuevoDepartamento != null && !nuevoDepartamento.isBlank()) {
                        tramite.setDepartamentoActual(nuevoDepartamento);
                }
                tramite.setActualizadoEn(LocalDateTime.now());

                if (nuevoEstado == Tramite.EstadoTramite.COMPLETADO || nuevoEstado == Tramite.EstadoTramite.RECHAZADO) {
                        tramite.setFinalizadoEn(LocalDateTime.now());
                }

                Tramite tramiteGuardado = tramiteRepository.save(tramite);
                
                // Notificar por WebSocket
                if (nuevoEstado == Tramite.EstadoTramite.COMPLETADO) {
                        webSocketNotificationService.notificarTramiteCompletado(tramiteGuardado);
                        webSocketNotificationService.notificarCuelloBottellaActualizado();
                } else {
                        webSocketNotificationService.notificarCambioTramite(tramiteGuardado);
                }

                return tramiteGuardado;
        }

        private String obtenerNombreNodoPorEstado(Tramite.EstadoTramite estado) {
                return switch (estado) {
                        case NUEVO -> "Inicio del trámite";
                        case EN_PROCESO -> "Procesando solicitud";
                        case PENDIENTE -> "En espera de información";
                        case EN_MORA -> "Trámite demorado";
                        case COMPLETADO -> "Trámite completado";
                        case RECHAZADO -> "Trámite rechazado";
                };
        }

    // ── Avanzar el trámite al siguiente paso ─────────────────────
        public Tramite avanzarTramite(String tramiteId, String funcionarioId,
                                                                  String comentario, Map<String, Object> datos,
                                                                  String siguienteNodoIdSolicitado,
                                                                  String departamentoDestino) {

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

        validarCamposRequeridos(nodoActual, datos);

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
            String siguienteNodoId = resolverSiguienteNodoId(
                    politica,
                    nodoActual,
                    siguienteNodoIdSolicitado,
                    departamentoDestino
            );
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
            tramite.setDepartamentoActual(siguienteNodo.getDepartamentoId());
            tramite.setEstado(Tramite.EstadoTramite.EN_PROCESO);

            // Notificar al siguiente departamento
            notificarDepartamento(tramite, siguienteNodo, "Trámite avanzado");

            // Notificar al cliente que avanzó
            notificarCliente(tramite, "Tu trámite avanzó",
                    "Tu trámite " + tramite.getCodigo() + " pasó a: " + siguienteNodo.getNombre(),
                    Notificacion.TipoNotificacion.TRAMITE_AVANZADO);
        }

        tramite.setActualizadoEn(LocalDateTime.now());
        Tramite tramiteGuardado = tramiteRepository.save(tramite);
        
        // Notificar cambios por WebSocket
        if (tramite.getEstado() == Tramite.EstadoTramite.COMPLETADO) {
            webSocketNotificationService.notificarTramiteCompletado(tramiteGuardado);
            webSocketNotificationService.notificarCuelloBottellaActualizado();
        } else {
            webSocketNotificationService.notificarCambioTramite(tramiteGuardado);
            webSocketNotificationService.notificarCuelloBottellaActualizado();
        }
        
        return tramiteGuardado;
    }

        private String resolverSiguienteNodoId(PoliticaNegocio politica,
                                                                                   PoliticaNegocio.Nodo nodoActual,
                                                                                   String siguienteNodoIdSolicitado,
                                                                                   String departamentoDestino) {
                List<String> siguientes = nodoActual.getSiguientes();
                if (siguientes == null || siguientes.isEmpty()) {
                        throw new RuntimeException("El nodo actual no tiene rutas siguientes");
                }

                if (siguientes.size() == 1) {
                        return siguientes.get(0);
                }

                if (siguienteNodoIdSolicitado != null && !siguienteNodoIdSolicitado.isBlank()) {
                        if (!siguientes.contains(siguienteNodoIdSolicitado)) {
                                throw new RuntimeException("El siguiente nodo seleccionado no es válido para este paso");
                        }
                        return siguienteNodoIdSolicitado;
                }

                if (departamentoDestino != null && !departamentoDestino.isBlank()) {
                        Optional<String> nodoPorDepartamento = siguientes.stream()
                                        .map(id -> buscarNodo(politica, id))
                                        .filter(n -> n.getDepartamentoId() != null
                                                        && n.getDepartamentoId().equalsIgnoreCase(departamentoDestino))
                                        .map(PoliticaNegocio.Nodo::getNodoId)
                                        .findFirst();

                        if (nodoPorDepartamento.isPresent()) {
                                return nodoPorDepartamento.get();
                        }
                }

                throw new RuntimeException("Este paso tiene múltiples rutas. Selecciona un departamento/nodo destino para continuar.");
        }

                private void validarCamposRequeridos(PoliticaNegocio.Nodo nodoActual, Map<String, Object> datos) {
                        List<String> campos = nodoActual.getCamposFormulario();
                        if (campos == null || campos.isEmpty()) {
                                return;
                        }

                        for (String campo : campos) {
                                Object valor = datos != null ? datos.get(campo) : null;
                                if (valor == null) {
                                        throw new RuntimeException("Campo requerido faltante: " + campo);
                                }

                                if (valor instanceof String str && str.isBlank()) {
                                        throw new RuntimeException("Campo requerido faltante: " + campo);
                                }
                        }
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

    // ── Obtener trámites por estado ──────────────────────────────
    public List<Tramite> obtenerPorEstado(Tramite.EstadoTramite estado) {
        return tramiteRepository.findByEstado(estado);
    }

    // ── Ver estado actual del trámite (para el cliente) ──────────
    public Map<String, Object> verEstadoTramite(String tramiteId) {

        Tramite tramite = tramiteRepository.findById(tramiteId)
                .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));

        Map<String, Object> estado = construirEstadoEjecucionBase(tramite);
        if (tramite.getPoliticaId() != null) {
            politicaRepository.findById(tramite.getPoliticaId()).ifPresent(politica ->
                    enriquecerEstadoConPolitica(estado, tramite, politica));
        }
        return estado;
    }

        // ── Vista de ejecución del trámite (motor + trazabilidad) ───
        public Map<String, Object> obtenerEstadoEjecucion(String tramiteId) {
                Tramite tramite = tramiteRepository.findById(tramiteId)
                                .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));

                Map<String, Object> estado = construirEstadoEjecucionBase(tramite);
                if (tramite.getPoliticaId() != null) {
                        politicaRepository.findById(tramite.getPoliticaId()).ifPresent(politica -> {
                                enriquecerEstadoConPolitica(estado, tramite, politica);
                        });
                }

                return estado;
        }

        // ── Generar PDF de cierre del trámite ───────────────────────
        public byte[] generarPdfCierre(String tramiteId) {
                Tramite tramite = tramiteRepository.findById(tramiteId)
                                .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));

                if (tramite.getEstado() != Tramite.EstadoTramite.COMPLETADO) {
                        throw new RuntimeException("El trámite aún no está finalizado");
                }

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                Document document = new Document();

                try {
                        PdfWriter.getInstance(document, output);
                        document.open();

                        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                        Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

                        document.add(new Paragraph("Comprobante de Trámite Finalizado", titulo));
                        document.add(new Paragraph(" "));
                        document.add(new Paragraph("Código: " + nullSafe(tramite.getCodigo()), normal));
                        document.add(new Paragraph("Estado: " + nullSafe(String.valueOf(tramite.getEstado())), normal));
                        document.add(new Paragraph("Cliente: " + nullSafe(tramite.getClienteNombre()), normal));
                        document.add(new Paragraph("Email cliente: " + nullSafe(tramite.getClienteEmail()), normal));
                        document.add(new Paragraph("Departamento actual: " + nullSafe(tramite.getDepartamentoActual()), normal));
                        document.add(new Paragraph("Fecha creación: " + nullSafe(String.valueOf(tramite.getCreadoEn())), normal));
                        document.add(new Paragraph("Fecha finalización: " + nullSafe(String.valueOf(tramite.getFinalizadoEn())), normal));
                        document.add(new Paragraph(" "));

                        document.add(new Paragraph("Historial de pasos", subtitulo));
                        if (tramite.getHistorial() == null || tramite.getHistorial().isEmpty()) {
                                document.add(new Paragraph("Sin historial registrado.", normal));
                        } else {
                                int index = 1;
                                for (Tramite.HistorialPaso paso : tramite.getHistorial()) {
                                        document.add(new Paragraph(index + ". "
                                                        + nullSafe(paso.getNombreNodo())
                                                        + " [" + nullSafe(String.valueOf(paso.getEstado())) + "]"
                                                        + " - Departamento: " + nullSafe(paso.getDepartamentoId()), normal));
                                        index++;
                                }
                        }

                        document.add(new Paragraph(" "));
                        document.add(new Paragraph("Datos de formulario", subtitulo));
                        if (tramite.getDatosFormulario() == null || tramite.getDatosFormulario().isEmpty()) {
                                document.add(new Paragraph("Sin datos capturados.", normal));
                        } else {
                                for (Map.Entry<String, Object> entry : tramite.getDatosFormulario().entrySet()) {
                                        document.add(new Paragraph(entry.getKey() + ": " + nullSafe(String.valueOf(entry.getValue())), normal));
                                }
                        }

                        document.close();
                        return output.toByteArray();
                } catch (Exception e) {
                        throw new RuntimeException("No se pudo generar el PDF del trámite", e);
                } finally {
                        if (document.isOpen()) {
                                document.close();
                        }
                }
        }

    // ── Métodos auxiliares ───────────────────────────────────────

    private PoliticaNegocio.Nodo buscarNodo(PoliticaNegocio politica, String nodoId) {
        return politica.getFlujo().stream()
                .filter(n -> n.getNodoId().equals(nodoId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nodo no encontrado: " + nodoId));
    }

        private int calcularTotalNodos(PoliticaNegocio politica) {
                return (int) politica.getFlujo().stream()
                                .filter(n -> n.getTipo() != PoliticaNegocio.Nodo.TipoNodo.INICIO
                                                && n.getTipo() != PoliticaNegocio.Nodo.TipoNodo.FIN)
                                .count();
        }

        private int calcularNodosCompletados(Tramite tramite) {
                return (int) tramite.getHistorial().stream()
                                .filter(p -> p.getEstado() == Tramite.HistorialPaso.EstadoPaso.COMPLETADO)
                                .count();
        }

        private Map<String, Object> construirEstadoEjecucionBase(Tramite tramite) {
                Map<String, Object> estado = new HashMap<>();
                estado.put("id", tramite.getId());
                estado.put("codigo", tramite.getCodigo());
                estado.put("estadoActual", tramite.getEstado());
                estado.put("nodoActualId", tramite.getNodoActualId());
                estado.put("nodoActualNombre", obtenerNombreNodoActual(tramite));
                estado.put("departamentoActual", tramite.getDepartamentoActual());
                estado.put("historial", tramite.getHistorial() == null ? List.of() : tramite.getHistorial());
                estado.put("datosFormulario", tramite.getDatosFormulario() == null ? Map.of() : tramite.getDatosFormulario());
                estado.put("notificaciones", notificacionRepository.findByTramiteIdOrderByCreadoEnAsc(tramite.getId()));
                estado.put("creadoEn", tramite.getCreadoEn());
                estado.put("actualizadoEn", tramite.getActualizadoEn());
                estado.put("finalizadoEn", tramite.getFinalizadoEn());
                return estado;
        }

        private void enriquecerEstadoConPolitica(Map<String, Object> estado, Tramite tramite, PoliticaNegocio politica) {
                int totalNodos = calcularTotalNodos(politica);
                int nodosCompletados = calcularNodosCompletados(tramite);
                int porcentajeAvance = totalNodos > 0 ? (nodosCompletados * 100) / totalNodos : 0;

                estado.put("politicaId", politica.getId());
                estado.put("politicaNombre", politica.getNombre());
                estado.put("totalNodos", totalNodos);
                estado.put("nodosCompletados", nodosCompletados);
                estado.put("porcentajeAvance", porcentajeAvance);
                estado.put("porcentajeRestante", Math.max(0, 100 - porcentajeAvance));

                if (tramite.getNodoActualId() != null) {
                        PoliticaNegocio.Nodo nodoActual = resolverNodoActual(politica, tramite.getNodoActualId());
                        if (nodoActual != null) {
                                estado.put("nodoActual", nodoActual);
                                estado.put("nodoActualNombre", nodoActual.getNombre());
                                estado.put("nodosSiguientes", construirNodosSiguientes(politica, nodoActual));
                        }
                }
        }

        private List<Map<String, Object>> construirNodosSiguientes(PoliticaNegocio politica, PoliticaNegocio.Nodo nodoActual) {
                if (nodoActual.getSiguientes() == null || nodoActual.getSiguientes().isEmpty()) {
                        return List.of();
                }

                List<Map<String, Object>> resultado = new ArrayList<>();
                for (String siguienteId : nodoActual.getSiguientes()) {
                        PoliticaNegocio.Nodo siguiente = resolverNodoActual(politica, siguienteId);
                        if (siguiente == null) {
                                continue;
                        }

                        Map<String, Object> item = new HashMap<>();
                        item.put("nodoId", siguiente.getNodoId());
                        item.put("nombre", siguiente.getNombre());
                        item.put("departamentoId", siguiente.getDepartamentoId());
                        item.put("tipo", siguiente.getTipo());
                        resultado.add(item);
                }

                return resultado;
        }

        private PoliticaNegocio.Nodo resolverNodoActual(PoliticaNegocio politica, String nodoId) {
                try {
                        return buscarNodo(politica, nodoId);
                } catch (RuntimeException e) {
                        return null;
                }
        }

        private String obtenerNombreNodoActual(Tramite tramite) {
                if (tramite.getHistorial() == null || tramite.getHistorial().isEmpty()) {
                        return null;
                }

                if (tramite.getNodoActualId() != null) {
                        return tramite.getHistorial().stream()
                                        .filter(p -> tramite.getNodoActualId().equals(p.getNodoId()))
                                        .map(Tramite.HistorialPaso::getNombreNodo)
                                        .findFirst()
                                        .orElse(null);
                }

                return tramite.getHistorial().get(tramite.getHistorial().size() - 1).getNombreNodo();
        }

        private String nullSafe(String valor) {
                return valor == null || valor.isBlank() ? "N/D" : valor;
        }

        private void inicializarDesdePoliticaSiAplica(Tramite tramite) {
                if (tramite.getPoliticaId() == null || tramite.getPoliticaId().isBlank()) {
                        return;
                }

                PoliticaNegocio politica = politicaRepository.findById(tramite.getPoliticaId())
                                .orElseThrow(() -> new RuntimeException("Política no encontrada"));

                PoliticaNegocio.Nodo nodoInicio = politica.getFlujo().stream()
                                .filter(n -> n.getTipo() == PoliticaNegocio.Nodo.TipoNodo.INICIO)
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("La política no tiene nodo de inicio"));

                if (nodoInicio.getSiguientes() == null || nodoInicio.getSiguientes().isEmpty()) {
                        throw new RuntimeException("La política no tiene un nodo inicial ejecutable");
                }

                String primerNodoId = nodoInicio.getSiguientes().get(0);
                PoliticaNegocio.Nodo primerNodo = buscarNodo(politica, primerNodoId);

                Tramite.HistorialPaso primerPaso = new Tramite.HistorialPaso();
                primerPaso.setNodoId(primerNodo.getNodoId());
                primerPaso.setNombreNodo(primerNodo.getNombre());
                primerPaso.setDepartamentoId(primerNodo.getDepartamentoId());
                primerPaso.setEstado(Tramite.HistorialPaso.EstadoPaso.PENDIENTE);
                primerPaso.setIniciadoEn(LocalDateTime.now());

                if (tramite.getHistorial() == null) {
                        tramite.setHistorial(new ArrayList<>());
                }
                boolean yaInicializado = tramite.getNodoActualId() != null
                                && tramite.getHistorial().stream().anyMatch(h -> primerNodoId.equals(h.getNodoId()));

                if (!yaInicializado) {
                        tramite.setNodoActualId(primerNodoId);
                        tramite.setDepartamentoActual(primerNodo.getDepartamentoId());
                        tramite.setEstado(Tramite.EstadoTramite.EN_PROCESO);
                        tramite.getHistorial().add(primerPaso);
                }
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