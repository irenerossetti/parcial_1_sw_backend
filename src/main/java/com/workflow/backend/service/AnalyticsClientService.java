package com.workflow.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Servicio Cliente para consumir Analytics API (ML Service)
 */
@Service
public class AnalyticsClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsClientService.class);
    
    @Value("${ml.service.url:http://localhost:8001}")
    private String mlServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public AnalyticsClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Obtiene métricas del sistema
     */
    public Map<String, Object> getMetrics(String startDate, String endDate, String metricType) {
        try {
            logger.info("📊 Llamando ML Service para métricas");
            
            String url = mlServiceUrl + "/api/analytics/metrics?metric_type=" + metricType;
            
            if (startDate != null && !startDate.isEmpty()) {
                url += "&start_date=" + startDate;
            }
            if (endDate != null && !endDate.isEmpty()) {
                url += "&end_date=" + endDate;
            }
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            logger.info("✓ Métricas obtenidas");
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo métricas", e);
            throw new RuntimeException("Error getting metrics: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene configuración del dashboard
     */
    public Map<String, Object> getDashboard(String userId, String dashboardType) {
        try {
            logger.info("📈 Obteniendo dashboard: tipo={}", dashboardType);
            
            String url = mlServiceUrl + "/api/analytics/dashboard?user_id=" + userId + 
                        "&dashboard_type=" + dashboardType;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo dashboard", e);
            throw new RuntimeException("Error getting dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene tendencias
     */
    public Map<String, Object> getTrends(int periodDays) {
        try {
            logger.info("📈 Obteniendo tendencias: {} días", periodDays);
            
            String url = mlServiceUrl + "/api/analytics/trends?period_days=" + periodDays;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo tendencias", e);
            throw new RuntimeException("Error getting trends: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene KPIs
     */
    public Map<String, Object> calculateKPIs() {
        try {
            logger.info("🎯 Calculando KPIs");
            
            String url = mlServiceUrl + "/api/analytics/kpi";
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error calculando KPIs", e);
            throw new RuntimeException("Error calculating KPIs: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene performance metrics
     */
    public Map<String, Object> getPerformance() {
        try {
            logger.info("⚡ Obteniendo performance metrics");
            
            String url = mlServiceUrl + "/api/analytics/performance";
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error en performance metrics", e);
            throw new RuntimeException("Error getting performance: " + e.getMessage());
        }
    }
}
