package com.workflow.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificaciones")
public class Notificacion {

    @Id
    private String id;

    private String usuarioId;       // A quién va dirigida
    private String tramiteId;       // De qué trámite habla
    private String tramiteCodigo;   // Ej: "TRM-2026-0001"

    private String titulo;          // Ej: "Nuevo trámite asignado"
    private String mensaje;         // Descripción más detallada

    private TipoNotificacion tipo;

    private boolean leida = false;

    private LocalDateTime creadoEn = LocalDateTime.now();

    public enum TipoNotificacion {
        NUEVO_TRAMITE,       // Le llegó un trámite nuevo al funcionario
        TRAMITE_AVANZADO,    // El trámite pasó al siguiente paso
        TRAMITE_COMPLETADO,  // El trámite finalizó (para el cliente)
        TRAMITE_RECHAZADO,   // Fue rechazado
        ACCION_REQUERIDA     // El funcionario debe hacer algo
    }
}