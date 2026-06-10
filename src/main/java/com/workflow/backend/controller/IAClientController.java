package com.workflow.backend.controller;

import com.workflow.backend.service.IAClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * REST Controller para Integración con ML Service
 * Comunica con FastAPI en puerto 8001
 * 
 * Endpoints:
 * POST /api/ia/recommend-policy - Recomendar política
 * GET /api/ia/list-policies - Listar políticas
 */
@RestController
@RequestMapping("/api/ia")
@CrossOrigin(origins = "*")
public class IAClientController {
    
    private static final Logger logger = LoggerFactory.getLogger(IAClientController.class);
    
    @Autowired
    private IAClientService iaClientService;
    
    /**
     * POST /api/ia/recommend-policy
     * Recomienda una política basada en descripción del cliente
     */
    @PostMapping("/recommend-policy")
    public ResponseEntity<Map<String, Object>> recommendPolicy(
        @RequestParam("tramiteId") String tramiteId,
        @RequestParam("clientText") String clientText,
        @RequestParam("userId") String userId) {
        
        try {
            logger.info("🤖 Recomendando política: tramite={}", tramiteId);
            
            Map<String, Object> result = iaClientService.recommendPolicy(
                tramiteId,
                clientText,
                userId
            );
            
            logger.info("✓ Recomendación obtenida");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error recomendando política: {}", tramiteId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/ia/list-policies
     * Lista todas las políticas disponibles
     */
    @GetMapping("/list-policies")
    public ResponseEntity<Map<String, Object>> listPolicies() {
        try {
            logger.info("📋 Listando políticas disponibles");
            
            Map<String, Object> policies = iaClientService.listAvailablePolicies();
            
            return ResponseEntity.ok(policies);
            
        } catch (Exception e) {
            logger.error("❌ Error listando políticas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/ia/extract-requirements
     * Extrae requisitos de texto usando NLP
     */
    @PostMapping("/extract-requirements")
    public ResponseEntity<Map<String, Object>> extractRequirements(
        @RequestParam("text") String text,
        @RequestParam(value = "language", defaultValue = "es") String language) {
        
        try {
            logger.info("🔍 Extrayendo requisitos del texto");
            
            Map<String, Object> result = iaClientService.extractRequirements(text, language);
            
            logger.info("✓ Requisitos extraídos");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error extrayendo requisitos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/ia/analyze-risk
     * Analiza riesgo de un trámite
     */
    @PostMapping("/analyze-risk")
    public ResponseEntity<Map<String, Object>> analyzeRisk(
        @RequestParam("tramiteId") String tramiteId,
        @RequestParam("policyType") String policyType,
        @RequestBody Map<String, Object> tramiteData) {
        
        try {
            logger.info("🔴 Analizando riesgo: tramite={}", tramiteId);
            
            Map<String, Object> result = iaClientService.analyzeRisk(
                tramiteId,
                policyType,
                tramiteData
            );
            
            logger.info("✓ Análisis de riesgo completado");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error analizando riesgo: {}", tramiteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/ia/generate-report
     * Genera reporte dinámico
     */
    @PostMapping("/generate-report")
    public ResponseEntity<Map<String, Object>> generateReport(
        @RequestParam("tramiteId") String tramiteId,
        @RequestParam("clientName") String clientName,
        @RequestParam(value = "template", defaultValue = "STANDARD") String template,
        @RequestBody Map<String, Object> reportData) {
        
        try {
            logger.info("📊 Generando reporte: tramite={}, template={}", tramiteId, template);
            
            Map<String, Object> result = iaClientService.generateReport(
                tramiteId,
                clientName,
                template,
                reportData
            );
            
            logger.info("✓ Reporte generado");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error generando reporte: {}", tramiteId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/ia/health
     * Verifica que el ML Service esté disponible
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = iaClientService.checkMLServiceHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("❌ ML Service no disponible", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "DOWN");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
}
