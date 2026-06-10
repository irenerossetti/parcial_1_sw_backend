package com.workflow.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Servicio Cliente para Notificaciones (ML Service)
 */
@Service
public class NotificationsClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationsClientService.class);
    
    @Value("${ml.service.url:http://localhost:8001}")
    private String mlServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public NotificationsClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Obtiene notificaciones del usuario
     */
    public Map<String, Object> getUserNotifications(String userId, int limit, boolean unreadOnly) {
        try {
            logger.info("📬 Obteniendo notificaciones: usuario={}", userId);
            
            String url = mlServiceUrl + "/api/notifications/user/" + userId + 
                        "?limit=" + limit + "&unread_only=" + unreadOnly;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            logger.info("✓ Notificaciones obtenidas");
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo notificaciones", e);
            throw new RuntimeException("Error getting notifications: " + e.getMessage());
        }
    }
    
    /**
     * Marca notificación como leída
     */
    public Map<String, Object> markRead(String notificationId) {
        try {
            logger.info("✓ Marcando como leída: {}", notificationId);
            
            String url = mlServiceUrl + "/api/notifications/mark-read/" + notificationId;
            
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error marcando como leída", e);
            throw new RuntimeException("Error marking as read: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene preferencias del usuario
     */
    public Map<String, Object> getPreferences(String userId) {
        try {
            logger.info("⚙️  Obteniendo preferencias: {}", userId);
            
            String url = mlServiceUrl + "/api/notifications/preferences/" + userId;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo preferencias", e);
            throw new RuntimeException("Error getting preferences: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene WebHooks
     */
    public Map<String, Object> getWebhooks() {
        try {
            logger.info("🔗 Obteniendo WebHooks");
            
            String url = mlServiceUrl + "/api/notifications/webhooks";
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo WebHooks", e);
            throw new RuntimeException("Error getting webhooks: " + e.getMessage());
        }
    }
}
