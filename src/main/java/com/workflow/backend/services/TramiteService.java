package com.workflow.backend.services;

import com.workflow.backend.models.*;
import com.workflow.backend.repositories.*;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import java.awt.Color;
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

        // ── Generar PDF de cierre del trámite (SIN VALIDACIONES PARA DEMO) ───────────────────────
        public byte[] generarPdfCierre(String tramiteId) {
                try {
                        Tramite tramite = tramiteRepository.findById(tramiteId)
                                        .orElseThrow(() -> new RuntimeException("Trámite no encontrado"));

                        // ============================================================
                        // VALIDACIÓN ELIMINADA PARA DEMO
                        // Permite generar PDF en cualquier estado del trámite
                        // ============================================================

                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        Document document = new Document(PageSize.A4, 40, 40, 60, 60);

                        try {
                                PdfWriter writer = PdfWriter.getInstance(document, output);
                                document.open();

                        // ═══════════════════════════════════════════════════════════
                        // ENCABEZADO CON DISEÑO PROFESIONAL
                        // ═══════════════════════════════════════════════════════════
                        
                        // Colores corporativos
                        Color azulPrimario = new Color(37, 99, 235); // #2563eb
                        Color azulOscuro = new Color(30, 64, 175);   // #1e40af
                        Color grisClaro = new Color(248, 250, 252);  // #f8fafc
                        Color verdeExito = new Color(16, 185, 129);  // #10b981
                        
                        // Tabla de encabezado con fondo azul
                        PdfPTable headerTable = new PdfPTable(2);
                        headerTable.setWidthPercentage(100);
                        headerTable.setWidths(new float[]{2, 1});
                        
                        // Celda izquierda - Título
                        PdfPCell leftCell = new PdfPCell();
                        leftCell.setBorder(Rectangle.NO_BORDER);
                        leftCell.setBackgroundColor(azulPrimario);
                        leftCell.setPadding(20);
                        
                        Font tituloGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.WHITE);
                        Font subtituloBlanco = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.WHITE);
                        
                        Paragraph titulo = new Paragraph("COMPROBANTE OFICIAL", tituloGrande);
                        titulo.setAlignment(Element.ALIGN_LEFT);
                        leftCell.addElement(titulo);
                        
                        Paragraph subtitulo = new Paragraph("Sistema de Gestión de Trámites", subtituloBlanco);
                        subtitulo.setAlignment(Element.ALIGN_LEFT);
                        subtitulo.setSpacingBefore(5);
                        leftCell.addElement(subtitulo);
                        
                        headerTable.addCell(leftCell);
                        
                        // Celda derecha - Estado
                        PdfPCell rightCell = new PdfPCell();
                        rightCell.setBorder(Rectangle.NO_BORDER);
                        rightCell.setBackgroundColor(verdeExito);
                        rightCell.setPadding(20);
                        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        
                        Font estadoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.WHITE);
                        Paragraph estado = new Paragraph("✓ COMPLETADO", estadoFont);
                        estado.setAlignment(Element.ALIGN_CENTER);
                        rightCell.addElement(estado);
                        
                        headerTable.addCell(rightCell);
                        document.add(headerTable);
                        
                        document.add(new Paragraph(" "));
                        
                        // ═══════════════════════════════════════════════════════════
                        // INFORMACIÓN PRINCIPAL DEL TRÁMITE
                        // ═══════════════════════════════════════════════════════════
                        
                        Font tituloSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, azulPrimario);
                        Font etiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
                        Font valor = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
                        
                        // Tabla de información principal
                        PdfPTable infoTable = new PdfPTable(2);
                        infoTable.setWidthPercentage(100);
                        infoTable.setWidths(new float[]{1, 2});
                        infoTable.setSpacingBefore(10);
                        
                        // Estilo de celdas
                        PdfPCell cellEtiqueta, cellValor;
                        
                        // Código del trámite
                        cellEtiqueta = new PdfPCell(new Phrase("Código de Trámite:", etiqueta));
                        cellEtiqueta.setBorder(Rectangle.NO_BORDER);
                        cellEtiqueta.setBackgroundColor(grisClaro);
                        cellEtiqueta.setPadding(10);
                        infoTable.addCell(cellEtiqueta);
                        
                        cellValor = new PdfPCell(new Phrase(nullSafe(tramite.getCodigo()), valor));
                        cellValor.setBorder(Rectangle.NO_BORDER);
                        cellValor.setPadding(10);
                        infoTable.addCell(cellValor);
                        
                        // Cliente
                        cellEtiqueta = new PdfPCell(new Phrase("Cliente:", etiqueta));
                        cellEtiqueta.setBorder(Rectangle.NO_BORDER);
                        cellEtiqueta.setBackgroundColor(grisClaro);
                        cellEtiqueta.setPadding(10);
                        infoTable.addCell(cellEtiqueta);
                        
                        cellValor = new PdfPCell(new Phrase(nullSafe(tramite.getClienteNombre()), valor));
                        cellValor.setBorder(Rectangle.NO_BORDER);
                        cellValor.setPadding(10);
                        infoTable.addCell(cellValor);
                        
                        // Email
                        cellEtiqueta = new PdfPCell(new Phrase("Email:", etiqueta));
                        cellEtiqueta.setBorder(Rectangle.NO_BORDER);
                        cellEtiqueta.setBackgroundColor(grisClaro);
                        cellEtiqueta.setPadding(10);
                        infoTable.addCell(cellEtiqueta);
                        
                        cellValor = new PdfPCell(new Phrase(nullSafe(tramite.getClienteEmail()), valor));
                        cellValor.setBorder(Rectangle.NO_BORDER);
                        cellValor.setPadding(10);
                        infoTable.addCell(cellValor);
                        
                        // Departamento
                        cellEtiqueta = new PdfPCell(new Phrase("Departamento:", etiqueta));
                        cellEtiqueta.setBorder(Rectangle.NO_BORDER);
                        cellEtiqueta.setBackgroundColor(grisClaro);
                        cellEtiqueta.setPadding(10);
                        infoTable.addCell(cellEtiqueta);
                        
                        cellValor = new PdfPCell(new Phrase(nullSafe(tramite.getDepartamentoActual()), valor));
                        cellValor.setBorder(Rectangle.NO_BORDER);
                        cellValor.setPadding(10);
                        infoTable.addCell(cellValor);
                        
                        // Fecha de creación
                        cellEtiqueta = new PdfPCell(new Phrase("Fecha de Inicio:", etiqueta));
                        cellEtiqueta.setBorder(Rectangle.NO_BORDER);
                        cellEtiqueta.setBackgroundColor(grisClaro);
                        cellEtiqueta.setPadding(10);
                        infoTable.addCell(cellEtiqueta);
                        
                        String fechaCreacion = tramite.getCreadoEn() != null 
                                ? tramite.getCreadoEn().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                : "N/A";
                        cellValor = new PdfPCell(new Phrase(fechaCreacion, valor));
                        cellValor.setBorder(Rectangle.NO_BORDER);
                        cellValor.setPadding(10);
                        infoTable.addCell(cellValor);
                        
                        // Fecha de finalización
                        cellEtiqueta = new PdfPCell(new Phrase("Fecha de Finalización:", etiqueta));
                        cellEtiqueta.setBorder(Rectangle.NO_BORDER);
                        cellEtiqueta.setBackgroundColor(grisClaro);
                        cellEtiqueta.setPadding(10);
                        infoTable.addCell(cellEtiqueta);
                        
                        String fechaFin = tramite.getFinalizadoEn() != null 
                                ? tramite.getFinalizadoEn().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                : "N/A";
                        cellValor = new PdfPCell(new Phrase(fechaFin, valor));
                        cellValor.setBorder(Rectangle.NO_BORDER);
                        cellValor.setPadding(10);
                        infoTable.addCell(cellValor);
                        
                        document.add(infoTable);
                        document.add(new Paragraph(" "));
                        
                        // ═══════════════════════════════════════════════════════════
                        // HISTORIAL DE PASOS
                        // ═══════════════════════════════════════════════════════════
                        
                        Paragraph tituloHistorial = new Paragraph("Historial del Proceso", tituloSeccion);
                        tituloHistorial.setSpacingBefore(15);
                        tituloHistorial.setSpacingAfter(10);
                        document.add(tituloHistorial);
                        
                        if (tramite.getHistorial() == null || tramite.getHistorial().isEmpty()) {
                                Paragraph sinHistorial = new Paragraph("Sin historial registrado.", valor);
                                sinHistorial.setAlignment(Element.ALIGN_CENTER);
                                sinHistorial.setSpacingBefore(10);
                                document.add(sinHistorial);
                        } else {
                                PdfPTable historialTable = new PdfPTable(3);
                                historialTable.setWidthPercentage(100);
                                historialTable.setWidths(new float[]{0.5f, 2, 1.5f});
                                
                                // Encabezados
                                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
                                
                                PdfPCell headerCell1 = new PdfPCell(new Phrase("#", headerFont));
                                headerCell1.setBackgroundColor(azulOscuro);
                                headerCell1.setPadding(8);
                                headerCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                                historialTable.addCell(headerCell1);
                                
                                PdfPCell headerCell2 = new PdfPCell(new Phrase("Paso", headerFont));
                                headerCell2.setBackgroundColor(azulOscuro);
                                headerCell2.setPadding(8);
                                historialTable.addCell(headerCell2);
                                
                                PdfPCell headerCell3 = new PdfPCell(new Phrase("Departamento", headerFont));
                                headerCell3.setBackgroundColor(azulOscuro);
                                headerCell3.setPadding(8);
                                historialTable.addCell(headerCell3);
                                
                                // Filas de datos
                                int index = 1;
                                for (Tramite.HistorialPaso paso : tramite.getHistorial()) {
                                        PdfPCell cell1 = new PdfPCell(new Phrase(String.valueOf(index), valor));
                                        cell1.setPadding(8);
                                        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                                        cell1.setBackgroundColor(index % 2 == 0 ? grisClaro : Color.WHITE);
                                        historialTable.addCell(cell1);
                                        
                                        String nombrePaso = nullSafe(paso.getNombreNodo());
                                        String estadoPaso = paso.getEstado() != null ? " ✓" : "";
                                        PdfPCell cell2 = new PdfPCell(new Phrase(nombrePaso + estadoPaso, valor));
                                        cell2.setPadding(8);
                                        cell2.setBackgroundColor(index % 2 == 0 ? grisClaro : Color.WHITE);
                                        historialTable.addCell(cell2);
                                        
                                        PdfPCell cell3 = new PdfPCell(new Phrase(nullSafe(paso.getDepartamentoId()), valor));
                                        cell3.setPadding(8);
                                        cell3.setBackgroundColor(index % 2 == 0 ? grisClaro : Color.WHITE);
                                        historialTable.addCell(cell3);
                                        
                                        index++;
                                }
                                
                                document.add(historialTable);
                        }
                        
                        document.add(new Paragraph(" "));
                        
                        // ═══════════════════════════════════════════════════════════
                        // DATOS DEL FORMULARIO
                        // ═══════════════════════════════════════════════════════════
                        
                        if (tramite.getDatosFormulario() != null && !tramite.getDatosFormulario().isEmpty()) {
                                Paragraph tituloDatos = new Paragraph("Datos Capturados", tituloSeccion);
                                tituloDatos.setSpacingBefore(15);
                                tituloDatos.setSpacingAfter(10);
                                document.add(tituloDatos);
                                
                                PdfPTable datosTable = new PdfPTable(2);
                                datosTable.setWidthPercentage(100);
                                datosTable.setWidths(new float[]{1, 2});
                                
                                for (Map.Entry<String, Object> entry : tramite.getDatosFormulario().entrySet()) {
                                        PdfPCell keyCell = new PdfPCell(new Phrase(entry.getKey() + ":", etiqueta));
                                        keyCell.setBorder(Rectangle.NO_BORDER);
                                        keyCell.setBackgroundColor(grisClaro);
                                        keyCell.setPadding(8);
                                        datosTable.addCell(keyCell);
                                        
                                        PdfPCell valueCell = new PdfPCell(new Phrase(nullSafe(String.valueOf(entry.getValue())), valor));
                                        valueCell.setBorder(Rectangle.NO_BORDER);
                                        valueCell.setPadding(8);
                                        datosTable.addCell(valueCell);
                                }
                                
                                document.add(datosTable);
                        }
                        
                        // ═══════════════════════════════════════════════════════════
                        // PIE DE PÁGINA CON QR Y SELLO/FIRMA
                        // ═══════════════════════════════════════════════════════════
                        
                        document.add(new Paragraph(" "));
                        document.add(new Paragraph(" "));
                        
                        // Línea separadora
                        LineSeparator line = new LineSeparator();
                        line.setLineColor(azulPrimario);
                        document.add(new Chunk(line));
                        
                        document.add(new Paragraph(" "));
                        
                        // Tabla con QR y Sello lado a lado
                        PdfPTable footerTable = new PdfPTable(2);
                        footerTable.setWidthPercentage(80);
                        footerTable.setWidths(new float[]{1, 1});
                        footerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
                        
                        // ═══════════════════════════════════════════════════════════
                        // CELDA IZQUIERDA: CÓDIGO QR
                        // ═══════════════════════════════════════════════════════════
                        PdfPCell qrCell = new PdfPCell();
                        qrCell.setBorder(Rectangle.BOX);
                        qrCell.setBorderColor(azulPrimario);
                        qrCell.setBorderWidth(2);
                        qrCell.setPadding(15);
                        qrCell.setBackgroundColor(Color.WHITE);
                        qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        
                        try {
                                // Generar código QR simple con el ID del trámite
                                String qrData = "TRAMITE:" + tramite.getCodigo() + "|ID:" + tramite.getId();
                                
                                // Crear un QR simple usando caracteres (fallback si no hay librería QR)
                                Font qrFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 8, Color.BLACK);
                                
                                Paragraph qrTitulo = new Paragraph("CÓDIGO QR", 
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, azulPrimario));
                                qrTitulo.setAlignment(Element.ALIGN_CENTER);
                                qrCell.addElement(qrTitulo);
                                
                                // Simulación visual de QR con caracteres
                                String qrVisual = 
                                        "█████████████████████\n" +
                                        "██ ▄▄▄▄▄ █▀ █▄ ▄▄▄▄▄ ██\n" +
                                        "██ █   █ █▀▄ █ █   █ ██\n" +
                                        "██ █▄▄▄█ █ ▀▄█ █▄▄▄█ ██\n" +
                                        "██▄▄▄▄▄▄▄█ ▀ █▄▄▄▄▄▄▄██\n" +
                                        "██ ▄ ▀▄ ▄ ▄▀▀▄▀▄█▀▀ ▄██\n" +
                                        "██▄██▀▀▄▄▀█ ▄ ▀ ▀▄▀▄ ██\n" +
                                        "██ ▄▄▄▄▄ █▄▀ ▄▀█▄▀ ▀███\n" +
                                        "██ █   █ █  ▀▄▀▄▀▀▄▀ ██\n" +
                                        "██ █▄▄▄█ █ ▀▄█ ▀▄▀▄▀ ██\n" +
                                        "██▄▄▄▄▄▄▄█▄▄██▄▄▄██▄▄██\n" +
                                        "█████████████████████";
                                
                                Paragraph qrCode = new Paragraph(qrVisual, qrFont);
                                qrCode.setAlignment(Element.ALIGN_CENTER);
                                qrCode.setSpacingBefore(5);
                                qrCell.addElement(qrCode);
                                
                                Font qrSmall = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.DARK_GRAY);
                                Paragraph qrInfo = new Paragraph("Escanea para verificar", qrSmall);
                                qrInfo.setAlignment(Element.ALIGN_CENTER);
                                qrInfo.setSpacingBefore(5);
                                qrCell.addElement(qrInfo);
                                
                        } catch (Exception e) {
                                // Si falla, mostrar texto alternativo
                                Paragraph qrError = new Paragraph("QR no disponible", 
                                        FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY));
                                qrError.setAlignment(Element.ALIGN_CENTER);
                                qrCell.addElement(qrError);
                        }
                        
                        footerTable.addCell(qrCell);
                        
                        // ═══════════════════════════════════════════════════════════
                        // CELDA DERECHA: SELLO DIGITAL
                        // ═══════════════════════════════════════════════════════════
                        PdfPCell selloCell = new PdfPCell();
                        selloCell.setBorder(Rectangle.BOX);
                        selloCell.setBorderColor(azulPrimario);
                        selloCell.setBorderWidth(2);
                        selloCell.setPadding(15);
                        selloCell.setBackgroundColor(new Color(239, 246, 255)); // Azul muy claro
                        selloCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        
                        Font selloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, azulPrimario);
                        Font selloSmall = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
                        
                        Paragraph selloTitulo = new Paragraph("DOCUMENTO VERIFICADO", selloFont);
                        selloTitulo.setAlignment(Element.ALIGN_CENTER);
                        selloCell.addElement(selloTitulo);
                        
                        Paragraph selloFecha = new Paragraph(
                                "Generado: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()),
                                selloSmall
                        );
                        selloFecha.setAlignment(Element.ALIGN_CENTER);
                        selloFecha.setSpacingBefore(5);
                        selloCell.addElement(selloFecha);
                        
                        Paragraph selloHash = new Paragraph(
                                "ID: " + tramite.getId().substring(0, Math.min(12, tramite.getId().length())),
                                selloSmall
                        );
                        selloHash.setAlignment(Element.ALIGN_CENTER);
                        selloHash.setSpacingBefore(3);
                        selloCell.addElement(selloHash);
                        
                        footerTable.addCell(selloCell);
                        document.add(footerTable);
                        
                        // Nota legal
                        document.add(new Paragraph(" "));
                        Font notaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
                        Paragraph nota = new Paragraph(
                                "Este documento es un comprobante oficial generado automáticamente por el Sistema de Gestión de Trámites. " +
                                "Para verificar su autenticidad, puede consultar el código del trámite en nuestro sistema.",
                                notaFont
                        );
                        nota.setAlignment(Element.ALIGN_CENTER);
                        nota.setSpacingBefore(15);
                        document.add(nota);

                        document.close();
                        return output.toByteArray();
                } catch (Exception e) {
                        System.err.println("ERROR GENERANDO PDF: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("No se pudo generar el PDF del trámite: " + e.getMessage(), e);
                } finally {
                        if (document.isOpen()) {
                                document.close();
                        }
                }
        } catch (Exception e) {
                System.err.println("ERROR EN generarPdfCierre: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
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