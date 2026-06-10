package com.workflow.backend.controller;

import com.workflow.backend.service.NotificationsClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * REST Controller para Notificaciones (Sprint 3)
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationsController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationsController.class);
    
    @Autowired
    private NotificationsClientService notificationsService;
    
    /**
     * GET /api/notifications/user/{userId}
     * Obtiene notificaciones del usuario
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserNotifications(
        @PathVariable String userId,
        @RequestParam(value = "limit", defaultValue = "20") int limit,
        @RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly) {
        
        try {
            logger.info("📬 Obteniendo notificaciones del usuario: {}", userId);
            
            Map<String, Object> notifications = notificationsService.getUserNotifications(userId, limit, unreadOnly);
            
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo notificaciones", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/notifications/mark-read/{notificationId}
     * Marca notificación como leída
     */
    @PostMapping("/mark-read/{notificationId}")
    public ResponseEntity<Map<String, Object>> markNotificationRead(
        @PathVariable String notificationId) {
        
        try {
            logger.info("✓ Marcando notificación como leída: {}", notificationId);
            
            Map<String, Object> result = notificationsService.markRead(notificationId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error marcando notificación", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/notifications/preferences/{userId}
     * Obtiene preferencias de notificación
     */
    @GetMapping("/preferences/{userId}")
    public ResponseEntity<Map<String, Object>> getPreferences(
        @PathVariable String userId) {
        
        try {
            logger.info("⚙️  Obteniendo preferencias: {}", userId);
            
            Map<String, Object> preferences = notificationsService.getPreferences(userId);
            
            return ResponseEntity.ok(preferences);
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo preferencias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/notifications/webhooks
     * Lista WebHooks registrados
     */
    @GetMapping("/webhooks")
    public ResponseEntity<Map<String, Object>> listWebhooks() {
        
        try {
            logger.info("🔗 Listando WebHooks");
            
            Map<String, Object> webhooks = notificationsService.getWebhooks();
            
            return ResponseEntity.ok(webhooks);
            
        } catch (Exception e) {
            logger.error("❌ Error listando WebHooks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
