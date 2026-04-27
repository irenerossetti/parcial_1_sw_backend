package com.workflow.backend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "tramites")
public class Tramite {

    @Id
    private String id;
    private String codigo;
    private String clienteId;
    private String clienteEmail;  // ← AGREGAR ESTE CAMPO
    private String clienteNombre;
    private String politicaId;
    private EstadoTramite estado;
    private String nodoActualId;
    private List<HistorialPaso> historial;
    private Map<String, Object> datosFormulario;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private LocalDateTime finalizadoEn;
    private String departamentoActual;  // ← MOVER AQUÍ, DENTRO DE LA CLASE

    // Enums internos
    public enum EstadoTramite {
        NUEVO,
        EN_PROCESO,
        COMPLETADO,
        RECHAZADO,
        PENDIENTE,      // ← AGREGAR
        EN_MORA,
    }

    // Clase interna para historial de pasos
    public static class HistorialPaso {
        private String nodoId;
        private String nombreNodo;
        private String departamentoId;
        private String funcionarioId;
        private String comentario;
        private Map<String, Object> datosIngresados;
        private EstadoPaso estado;
        private LocalDateTime iniciadoEn;
        private LocalDateTime completadoEn;

        public enum EstadoPaso {
            PENDIENTE,
            COMPLETADO,
            RECHAZADO
        }

        // Getters y Setters
        public String getNodoId() { return nodoId; }
        public void setNodoId(String nodoId) { this.nodoId = nodoId; }

        public String getNombreNodo() { return nombreNodo; }
        public void setNombreNodo(String nombreNodo) { this.nombreNodo = nombreNodo; }

        public String getDepartamentoId() { return departamentoId; }
        public void setDepartamentoId(String departamentoId) { this.departamentoId = departamentoId; }

        public String getFuncionarioId() { return funcionarioId; }
        public void setFuncionarioId(String funcionarioId) { this.funcionarioId = funcionarioId; }

        public String getComentario() { return comentario; }
        public void setComentario(String comentario) { this.comentario = comentario; }

        public Map<String, Object> getDatosIngresados() { return datosIngresados; }
        public void setDatosIngresados(Map<String, Object> datosIngresados) { this.datosIngresados = datosIngresados; }

        public EstadoPaso getEstado() { return estado; }
        public void setEstado(EstadoPaso estado) { this.estado = estado; }

        public LocalDateTime getIniciadoEn() { return iniciadoEn; }
        public void setIniciadoEn(LocalDateTime iniciadoEn) { this.iniciadoEn = iniciadoEn; }

        public LocalDateTime getCompletadoEn() { return completadoEn; }
        public void setCompletadoEn(LocalDateTime completadoEn) { this.completadoEn = completadoEn; }
    }

    // Constructores
    public Tramite() {
        this.historial = new ArrayList<>();
        this.datosFormulario = new HashMap<>();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getClienteEmail() { return clienteEmail; }  // ← AGREGAR
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }  // ← AGREGAR

    public String getClienteNombre() { return clienteNombre; }  // ← AGREGAR
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }  // ← AGREGAR

    public String getPoliticaId() { return politicaId; }
    public void setPoliticaId(String politicaId) { this.politicaId = politicaId; }

    public EstadoTramite getEstado() { return estado; }
    public void setEstado(EstadoTramite estado) { this.estado = estado; }

    public String getNodoActualId() { return nodoActualId; }
    public void setNodoActualId(String nodoActualId) { this.nodoActualId = nodoActualId; }

    public List<HistorialPaso> getHistorial() { return historial; }
    public void setHistorial(List<HistorialPaso> historial) { this.historial = historial; }

    public Map<String, Object> getDatosFormulario() { return datosFormulario; }
    public void setDatosFormulario(Map<String, Object> datosFormulario) { this.datosFormulario = datosFormulario; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public LocalDateTime getFinalizadoEn() { return finalizadoEn; }
    public void setFinalizadoEn(LocalDateTime finalizadoEn) { this.finalizadoEn = finalizadoEn; }

    public String getDepartamentoActual() { return departamentoActual; }
    public void setDepartamentoActual(String departamentoActual) { this.departamentoActual = departamentoActual; }
}