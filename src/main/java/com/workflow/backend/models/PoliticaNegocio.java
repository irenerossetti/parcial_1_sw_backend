package com.workflow.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "politicas_negocio")
public class PoliticaNegocio {

    @Id
    private String id;

    private String nombre;          // Ej: "Instalación de Luz - CRE"
    private String descripcion;

    private String empresaId;       // A qué empresa/organización pertenece

    private List<Nodo> flujo;       // Lista de pasos/nodos del diagrama

    private TipoFlujo tipoFlujo;   // Qué tipo de flujo es

    private boolean activo = true;

    private LocalDateTime creadoEn = LocalDateTime.now();
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    // ── Tipos de flujo ──────────────────────────────────────────
    public enum TipoFlujo {
        LINEAL,        // A → B → C (siempre igual)
        ALTERNATIVO,   // A → B o A → C (depende de condición)
        INTERACTIVO,   // Varias tareas en paralelo
        PROCESO        // Simultáneo entre departamentos
    }

    // ── Cada nodo/paso del diagrama ──────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Nodo {

        private String nodoId;          // ID único del nodo (ej: "nodo_1")
        private String nombre;          // Ej: "Revisión Legal"
        private String descripcion;

        private String departamentoId;  // Qué dpto ejecuta este paso
        private String responsableId;   // Quién específicamente (opcional)

        private TipoNodo tipo;          // Inicio, tarea, decisión, fin

        private List<String> siguientes; // IDs de nodos que siguen
        private String condicion;        // Para flujos alternativos

        private int ordenEjecucion;      // Para flujos lineales

        private List<String> camposFormulario; // Campos que debe llenar

        public enum TipoNodo {
            INICIO,
            TAREA,
            DECISION,   // Para flujos alternativos (if/else)
            PARALELO,   // Para flujos simultáneos
            FIN
        }
    }
}