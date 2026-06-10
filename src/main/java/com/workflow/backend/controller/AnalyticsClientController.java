package com.workflow.backend.controller;

import com.workflow.backend.service.AnalyticsClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * REST Controller para Analytics Dashboard (Sprint 3)
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsClientController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsClientController.class);
    
    @Autowired
    private AnalyticsClientService analyticsService;
    
    /**
     * GET /api/analytics/metrics
     * Obtiene métricas del sistema
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics(
        @RequestParam(value = "startDate", required = false) String startDate,
        @RequestParam(value = "endDate", required = false) String endDate,
        @RequestParam(value = "metricType", defaultValue = "all") String metricType) {
        
        try {
            logger.info("📊 Obteniendo métricas: tipo={}", metricType);
            
            Map<String, Object> metrics = analyticsService.getMetrics(startDate, endDate, metricType);
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo métricas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/analytics/dashboard
     * Obtiene configuración del dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
        @RequestParam("userId") String userId,
        @RequestParam(value = "type", defaultValue = "overview") String dashboardType) {
        
        try {
            logger.info("📈 Cargando dashboard: tipo={}", dashboardType);
            
            Map<String, Object> dashboard = analyticsService.getDashboard(userId, dashboardType);
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            logger.error("❌ Error cargando dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/analytics/trends
     * Obtiene tendencias
     */
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getTrends(
        @RequestParam(value = "periodDays", defaultValue = "30") int periodDays) {
        
        try {
            logger.info("📈 Obteniendo tendencias: período={} días", periodDays);
            
            Map<String, Object> trends = analyticsService.getTrends(periodDays);
            
            return ResponseEntity.ok(trends);
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo tendencias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/analytics/kpi
     * Obtiene KPIs principales
     */
    @GetMapping("/kpi")
    public ResponseEntity<Map<String, Object>> getKPIs() {
        
        try {
            logger.info("🎯 Calculando KPIs");
            
            Map<String, Object> kpis = analyticsService.calculateKPIs();
            
            return ResponseEntity.ok(kpis);
            
        } catch (Exception e) {
            logger.error("❌ Error calculando KPIs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/analytics/performance
     * Obtiene métricas de performance
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformance() {
        
        try {
            logger.info("⚡ Calculando performance metrics");
            
            Map<String, Object> performance = analyticsService.getPerformance();
            
            return ResponseEntity.ok(performance);
            
        } catch (Exception e) {
            logger.error("❌ Error en performance metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
