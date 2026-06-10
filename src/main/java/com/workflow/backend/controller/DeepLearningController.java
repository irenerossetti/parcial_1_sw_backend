package com.workflow.backend.controller;

import com.workflow.backend.service.DeepLearningClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * REST Controller para Deep Learning Models (Sprint 3)
 */
@RestController
@RequestMapping("/api/deeplearning")
@CrossOrigin(origins = "*")
public class DeepLearningController {
    
    private static final Logger logger = LoggerFactory.getLogger(DeepLearningController.class);
    
    @Autowired
    private DeepLearningClientService deepLearningService;
    
    /**
     * POST /api/deeplearning/predict
     * Realiza predicción con modelo
     */
    @PostMapping("/predict")
    public ResponseEntity<Map<String, Object>> predict(
        @RequestParam("modelId") String modelId,
        @RequestBody Map<String, Object> data) {
        
        try {
            logger.info("🔮 Realizando predicción: modelo={}", modelId);
            
            Map<String, Object> prediction = deepLearningService.predict(modelId, data);
            
            return ResponseEntity.ok(prediction);
            
        } catch (Exception e) {
            logger.error("❌ Error en predicción", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/deeplearning/models
     * Lista modelos disponibles
     */
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> listModels() {
        
        try {
            logger.info("📚 Listando modelos de Deep Learning");
            
            Map<String, Object> models = deepLearningService.listModels();
            
            return ResponseEntity.ok(models);
            
        } catch (Exception e) {
            logger.error("❌ Error listando modelos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/deeplearning/models/{modelId}
     * Obtiene información del modelo
     */
    @GetMapping("/models/{modelId}")
    public ResponseEntity<Map<String, Object>> getModelInfo(
        @PathVariable String modelId) {
        
        try {
            logger.info("📋 Obteniendo info del modelo: {}", modelId);
            
            Map<String, Object> modelInfo = deepLearningService.getModelInfo(modelId);
            
            return ResponseEntity.ok(modelInfo);
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo info del modelo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/deeplearning/models/{modelId}/deploy
     * Despliega modelo a producción
     */
    @PostMapping("/models/{modelId}/deploy")
    public ResponseEntity<Map<String, Object>> deployModel(
        @PathVariable String modelId,
        @RequestParam(value = "environment", defaultValue = "staging") String environment) {
        
        try {
            logger.info("🚀 Desplegando modelo: {} a {}", modelId, environment);
            
            Map<String, Object> result = deepLearningService.deployModel(modelId, environment);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error desplegando modelo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/deeplearning/batch-predict
     * Predicciones en lote
     */
    @PostMapping("/batch-predict")
    public ResponseEntity<Map<String, Object>> batchPredict(
        @RequestParam("modelId") String modelId,
        @RequestBody List<Map<String, Object>> data) {
        
        try {
            logger.info("🔮 Batch predict: {} muestras", data.size());
            
            Map<String, Object> predictions = deepLearningService.batchPredict(modelId, data);
            
            return ResponseEntity.ok(predictions);
            
        } catch (Exception e) {
            logger.error("❌ Error en batch predict", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/deeplearning/models/{modelId}/speed
     * Obtiene métricas de velocidad
     */
    @GetMapping("/models/{modelId}/speed")
    public ResponseEntity<Map<String, Object>> getInferenceSpeed(
        @PathVariable String modelId) {
        
        try {
            logger.info("⚡ Obteniendo velocidad de inferencia: {}", modelId);
            
            Map<String, Object> speedMetrics = deepLearningService.getInferenceSpeed(modelId);
            
            return ResponseEntity.ok(speedMetrics);
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo velocidad", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
