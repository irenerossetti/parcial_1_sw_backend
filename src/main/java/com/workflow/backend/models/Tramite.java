package com.workflow.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tramites")
public class Tramite {

    @Id
    private String id;

    private String codigo;           // Código legible: Ej: "TRM-2026-0001"

    private String clienteId;        // Usuario que inició el trámite
    private String politicaId;       // Qué política de negocio sigue

    private EstadoTramite estado;    // Estado general del trámite

    private String nodoActualId;     // En qué paso del flujo está AHORA

    private List<HistorialPaso> historial; // Todo lo que ha pasado

    private Map<String, Object> datosFormulario; // Datos ingresados por dptos

    private LocalDateTime creadoEn = LocalDateTime.now();
    private LocalDateTime actualizadoEn = LocalDateTime.now();
    private LocalDateTime finalizadoEn;

    // ── Estado general del trámite ───────────────────────────────
    public enum EstadoTramite {
        NUEVO,          // Etiqueta ROJA  - Recién llegó
        EN_PROCESO,     // Etiqueta AMARILLA - En ejecución
        COMPLETADO,     // Etiqueta VERDE - Finalizado
        RECHAZADO,      // Fue denegado
        PAUSADO         // En espera de algo
    }

    // ── Registro de cada paso que se completó ───────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistorialPaso {

        private String nodoId;
        private String nombreNodo;
        private String departamentoId;
        private String funcionarioId;    // Quién lo procesó

        private EstadoPaso estado;

        private String comentario;       // Notas del funcionario
        private Map<String, Object> datosIngresados; // Formulario llenado

        private LocalDateTime iniciadoEn;
        private LocalDateTime completadoEn;

        public enum EstadoPaso {
            PENDIENTE,
            EN_PROCESO,
            COMPLETADO,
            RECHAZADO
        }
    }
}