package com.workflow.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Servicio Cliente para consumir APIs del ML Service (FastAPI)
 * 
 * Realiza llamadas HTTP REST al servicio en puerto 8001
 */
@Service
public class IAClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(IAClientService.class);
    
    @Value("${ml.service.url:http://localhost:8001}")
    private String mlServiceUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public IAClientService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Recomienda una política basada en texto del cliente
     */
    public Map<String, Object> recommendPolicy(String tramiteId, String clientText, String userId) {
        try {
            logger.info("🤖 Llamando ML Service para recomendación: tramite={}", tramiteId);
            
            String url = mlServiceUrl + "/api/ia/policies/recommend";
            
            // Construir request
            Map<String, Object> request = new HashMap<>();
            request.put("clientText", clientText);
            request.put("tramiteId", tramiteId);
            request.put("userId", userId);
            request.put("clientAudio", null);
            
            // Realizar llamada HTTP
            Map<String, Object> response = callMLService(url, request, Map.class);
            
            logger.info("✓ Recomendación recibida: {} políticas", 
                ((List<?>) response.get("recommendations")).size());
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error llamando ML Service: {}", e.getMessage(), e);
            throw new RuntimeException("Error calling ML Service: " + e.getMessage());
        }
    }
    
    /**
     * Lista todas las políticas disponibles
     */
    public Map<String, Object> listAvailablePolicies() {
        try {
            logger.info("📋 Obteniendo lista de políticas del ML Service");
            
            String url = mlServiceUrl + "/api/ia/policies/list";
            
            Map<String, Object> response = callMLService(url, null, Map.class);
            
            logger.info("✓ Políticas obtenidas");
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error listando políticas: {}", e.getMessage());
            throw new RuntimeException("Error listing policies: " + e.getMessage());
        }
    }
    
    /**
     * Extrae requisitos de texto usando NLP
     */
    public Map<String, Object> extractRequirements(String text, String language) {
        try {
            logger.info("🔍 Extrayendo requisitos con NLP");
            
            String url = mlServiceUrl + "/api/nlp/extract-requirements";
            
            Map<String, Object> request = new HashMap<>();
            request.put("text", text);
            request.put("language", language);
            request.put("extractEntities", true);
            
            Map<String, Object> response = callMLService(url, request, Map.class);
            
            logger.info("✓ Requisitos extraídos");
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error extrayendo requisitos: {}", e.getMessage());
            throw new RuntimeException("Error extracting requirements: " + e.getMessage());
        }
    }
    
    /**
     * Analiza riesgo de un trámite
     */
    public Map<String, Object> analyzeRisk(String tramiteId, String policyType, Map<String, Object> tramiteData) {
        try {
            logger.info("🔴 Analizando riesgo del trámite: {}", tramiteId);
            
            String url = mlServiceUrl + "/api/riesgo/analyze";
            
            Map<String, Object> request = new HashMap<>();
            request.put("tramiteId", tramiteId);
            request.put("policyType", policyType);
            request.put("clientName", tramiteData.getOrDefault("clientName", ""));
            request.put("documents", tramiteData.getOrDefault("documents", new ArrayList<>()));
            request.put("clientHistory", tramiteData.getOrDefault("clientHistory", null));
            request.put("additionalInfo", tramiteData.getOrDefault("additionalInfo", null));
            
            Map<String, Object> response = callMLService(url, request, Map.class);
            
            logger.info("✓ Análisis de riesgo completado: nivel={}", 
                response.get("overallRiskLevel"));
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error analizando riesgo: {}", e.getMessage());
            throw new RuntimeException("Error analyzing risk: " + e.getMessage());
        }
    }
    
    /**
     * Genera reporte dinámico
     */
    public Map<String, Object> generateReport(String tramiteId, String clientName, 
                                              String template, Map<String, Object> reportData) {
        try {
            logger.info("📊 Generando reporte: tramite={}, template={}", tramiteId, template);
            
            String url = mlServiceUrl + "/api/reportes/generate";
            
            Map<String, Object> request = new HashMap<>();
            request.put("tramiteId", tramiteId);
            request.put("clientName", clientName);
            request.put("template", template);
            request.put("data", reportData);
            request.put("includeSignature", false);
            
            Map<String, Object> response = callMLService(url, request, Map.class);
            
            logger.info("✓ Reporte generado: {} páginas", response.get("pages"));
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error generando reporte: {}", e.getMessage());
            throw new RuntimeException("Error generating report: " + e.getMessage());
        }
    }
    
    /**
     * Verifica que el ML Service esté disponible
     */
    public Map<String, Object> checkMLServiceHealth() {
        try {
            logger.info("🏥 Verificando salud del ML Service");
            
            String url = mlServiceUrl + "/api/health";
            
            Map<String, Object> response = callMLService(url, null, Map.class);
            
            response.put("mlServiceUrl", mlServiceUrl);
            response.put("connected", true);
            
            logger.info("✓ ML Service disponible");
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ ML Service no disponible: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "DOWN");
            error.put("connected", false);
            error.put("error", e.getMessage());
            throw new RuntimeException("ML Service not available: " + e.getMessage());
        }
    }
    
    /**
     * Método genérico para llamadas HTTP al ML Service
     */
    private <T> T callMLService(String url, Object requestBody, Class<T> responseType) {
        try {
            logger.debug("HTTP POST: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            
            HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);
            
            T response = restTemplate.postForObject(url, entity, responseType);
            
            logger.debug("Respuesta recibida: {}", responseType.getSimpleName());
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error en llamada HTTP: {} - {}", url, e.getMessage());
            throw new RuntimeException("HTTP call failed: " + e.getMessage());
        }
    }
}
