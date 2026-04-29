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
    
    private List<Swimlane> swimlanes; // Carriles/particiones del diagrama
    
    private List<CampoFormulario> campos; // Campos dinámicos del formulario

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

    // ── Swimlane/Carril (partición) ──────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Swimlane {
        private String id;
        private String nombre;          // Ej: "Cliente", "Sistema", "Administración"
        private String departamentoId;  // Departamento asociado
        private String color;           // Color del carril
        private int orden;              // Orden de visualización
        private OrientacionSwimlane orientacion; // Horizontal o vertical
        
        public enum OrientacionSwimlane {
            HORIZONTAL,  // Carriles horizontales (uno encima del otro)
            VERTICAL     // Carriles verticales (uno al lado del otro)
        }
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

        private TipoNodo tipo;          // Tipo de nodo UML 2.5

        private List<String> siguientes; // IDs de nodos que siguen
        private String condicion;        // Para flujos alternativos

        private int ordenEjecucion;      // Para flujos lineales

        private List<String> camposFormulario; // Campos que debe llenar
        
        // ── Propiedades UML 2.5 adicionales ──────────────────────
        private String swimlane;         // Carril/partición donde está el nodo
        private String objetoEstado;     // Para nodos de objeto: estado del objeto
        private String señalTipo;        // Para nodos de señal: tipo de señal
        private String tiempoEspera;     // Para eventos de tiempo: duración
        private boolean esInterrumpible; // Si la región es interrumpible
        private String actividadLlamada; // ID de la actividad que se llama
        private TipoExpansion tipoExpansion; // Para regiones de expansión
        
        // Propiedades visuales
        private PosicionNodo posicion;   // Posición en el canvas
        private String color;            // Color personalizado del nodo
        private String icono;            // Icono opcional
        
        public enum TipoExpansion {
            ITERATIVA,      // <<iterative>>
            PARALELA,       // <<parallel>>
            STREAM          // <<stream>>
        }
    }
    
    // ── Posición del nodo en el canvas ──────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PosicionNodo {
        private double x;
        private double y;
        private double ancho;  // Opcional, para nodos con tamaño variable
        private double alto;   // Opcional
    }

        public enum TipoNodo {
            // Nodos básicos
            INICIO,              // Nodo inicial (círculo negro)
            FIN,                 // Nodo final (círculo con borde)
            TAREA,               // Actividad/Acción (rectángulo redondeado)
            
            // Nodos de control UML 2.5
            DECISION,            // Nodo de decisión (diamante) - bifurcación condicional
            MERGE,               // Nodo de merge (diamante) - unión de flujos
            FORK,                // Nodo de fork (barra horizontal) - inicio de paralelismo
            JOIN,                // Nodo de join (barra horizontal) - fin de paralelismo
            
            // Nodos de objeto y señal
            OBJETO,              // Nodo de objeto (rectángulo)
            SEÑAL_ENVIO,         // Envío de señal (pentágono convexo)
            SEÑAL_RECEPCION,     // Recepción de señal (pentágono cóncavo)
            EVENTO_TIEMPO,       // Evento basado en tiempo (reloj)
            
            // Nodos de actividad estructurada
            REGION_EXPANSION,    // Región de expansión (rectángulo con <<iterative>>)
            ACTIVIDAD_LLAMADA,   // Llamada a otra actividad (rectángulo con rake)
            
            // Nodos de excepción
            EVENTO_ACEPTACION,   // Aceptación de evento (pentágono cóncavo)
            INTERRUPCION         // Región interrumpible (rectángulo con esquinas redondeadas y borde discontinuo)
        }
    }
}