package com.workflow.backend.controllers;

import com.workflow.backend.models.Tramite;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Controlador WebSocket para notificaciones en tiempo real de cambios de trámites
 * Maneja la propagación de eventos a clientes conectados
 */
@Controller
public class WebSocketController {

    /**
     * DTO para notificación de cambio de estado de trámite
     */
    public static class TramiteNotification {
        public String tramiteId;
        public String codigo;
        public String estadoActual;
        public String departamentoActual;
        public String clienteEmail;
        public long timestamp;

        public TramiteNotification() {}

        public TramiteNotification(Tramite tramite) {
            this.tramiteId = tramite.getId();
            this.codigo = tramite.getCodigo();
            this.estadoActual = tramite.getEstado().toString();
            this.departamentoActual = tramite.getDepartamentoActual() != null ? 
                    tramite.getDepartamentoActual() : "Sin asignar";
            this.clienteEmail = tramite.getClienteEmail();
            this.timestamp = System.currentTimeMillis();
        }

        // Getters y Setters
        public String getTramiteId() { return tramiteId; }
        public void setTramiteId(String tramiteId) { this.tramiteId = tramiteId; }

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public String getEstadoActual() { return estadoActual; }
        public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }

        public String getDepartamentoActual() { return departamentoActual; }
        public void setDepartamentoActual(String departamentoActual) { 
            this.departamentoActual = departamentoActual; 
        }

        public String getClienteEmail() { return clienteEmail; }
        public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
