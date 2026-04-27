package com.workflow.backend.services;

import com.workflow.backend.controllers.WebSocketController.TramiteNotification;
import com.workflow.backend.models.Tramite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio para enviar notificaciones WebSocket en tiempo real
 * cuando cambien los estados de los trámites
 */
@Service
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Notifica a todos los clientes conectados sobre un cambio en un trámite
     * @param tramite El trámite que cambió
     */
    public void notificarCambioTramite(Tramite tramite) {
        if (tramite == null) return;
        
        TramiteNotification notification = new TramiteNotification(tramite);
        
        // Enviar a TODOS los conectados en el tema general
        messagingTemplate.convertAndSend(
            "/topic/tramites/actualizacion",
            notification
        );
        
        // Enviar también al cliente específico
        messagingTemplate.convertAndSend(
            "/topic/tramites/" + tramite.getClienteEmail(),
            notification
        );
    }

    /**
     * Notifica sobre un nuevo trámite creado
     */
    public void notificarNuevoTramite(Tramite tramite) {
        if (tramite == null) return;
        
        TramiteNotification notification = new TramiteNotification(tramite);
        
        messagingTemplate.convertAndSend(
            "/topic/tramites/nuevo",
            notification
        );
    }

    /**
     * Notifica cuando se completa un trámite
     */
    public void notificarTramiteCompletado(Tramite tramite) {
        if (tramite == null) return;
        
        TramiteNotification notification = new TramiteNotification(tramite);
        
        messagingTemplate.convertAndSend(
            "/topic/tramites/completado",
            notification
        );
        
        // También notificar al cliente específico
        messagingTemplate.convertAndSend(
            "/topic/tramites/" + tramite.getClienteEmail() + "/completado",
            notification
        );
    }

    /**
     * Notifica sobre cambios en cuellos de botella (para admin/funcionarios)
     */
    public void notificarCuelloBottellaActualizado() {
        messagingTemplate.convertAndSend(
            "/topic/analytics/cuellos-botella-actualizado",
            new Object() {
                public long timestamp = System.currentTimeMillis();
            }
        );
    }
}
