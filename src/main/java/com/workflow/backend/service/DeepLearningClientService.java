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
 * Servicio Cliente para Deep Learning Models (ML Service)
 */
@Service
public class DeepLearningClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeepLearningClientService.class);
    
    @Value("${ml.service.url:http://localhost:8001}")
    private String mlServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public DeepLearningClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Realiza predicción con modelo
     */
    public Map<String, Object> predict(String modelId, Map<String, Object> data) {
        try {
            logger.info("🔮 Prediciendo con modelo: {}", modelId);
            
            String url = mlServiceUrl + "/api/deeplearning/predict";
            
            Map<String, Object> request = new HashMap<>();
            request.put("data", data);
            request.put("modelId", modelId);
            request.put("modelVersion", "latest");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> entity = new HttpEntity<>(request, headers);
            
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            logger.info("✓ Predicción completada");
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error en predicción", e);
            throw new RuntimeException("Error predicting: " + e.getMessage());
        }
    }
    
    /**
     * Lista modelos disponibles
     */
    public Map<String, Object> listModels() {
        try {
            logger.info("📚 Listando modelos");
            
            String url = mlServiceUrl + "/api/deeplearning/models";
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error listando modelos", e);
            throw new RuntimeException("Error listing models: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene información del modelo
     */
    public Map<String, Object> getModelInfo(String modelId) {
        try {
            logger.info("📋 Obteniendo info del modelo: {}", modelId);
            
            String url = mlServiceUrl + "/api/deeplearning/models/" + modelId;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo info", e);
            throw new RuntimeException("Error getting model info: " + e.getMessage());
        }
    }
    
    /**
     * Despliega modelo
     */
    public Map<String, Object> deployModel(String modelId, String environment) {
        try {
            logger.info("🚀 Desplegando modelo: {} a {}", modelId, environment);
            
            String url = mlServiceUrl + "/api/deeplearning/models/" + modelId + "/deploy?environment=" + environment;
            
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
            
            logger.info("✓ Modelo desplegado");
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error desplegando", e);
            throw new RuntimeException("Error deploying model: " + e.getMessage());
        }
    }
    
    /**
     * Predicciones en lote
     */
    public Map<String, Object> batchPredict(String modelId, List<Map<String, Object>> data) {
        try {
            logger.info("🔮 Batch predict: {} muestras", data.size());
            
            String url = mlServiceUrl + "/api/deeplearning/batch-predict?model_id=" + modelId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> entity = new HttpEntity<>(data, headers);
            
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            logger.info("✓ Batch predict completado");
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error en batch predict", e);
            throw new RuntimeException("Error batch predicting: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene métricas de velocidad
     */
    public Map<String, Object> getInferenceSpeed(String modelId) {
        try {
            logger.info("⚡ Obteniendo velocidad: {}", modelId);
            
            String url = mlServiceUrl + "/api/deeplearning/inference-speed/" + modelId;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo velocidad", e);
            throw new RuntimeException("Error getting speed: " + e.getMessage());
        }
    }
}
