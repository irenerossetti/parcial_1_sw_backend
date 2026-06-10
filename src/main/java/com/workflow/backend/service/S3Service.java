package com.workflow.backend.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

// AWS SDK v2 S3
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * Servicio para interactuar con S3 de AWS.
 * Si las credenciales o el nombre del bucket no están configurados, 
 * cambia automáticamente al modo LOCAL FALLBACK.
 */
@Service
public class S3Service {
    
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    
    @Value("${aws.s3.bucketName:}")
    private String bucketName;
    
    @Autowired(required = false)
    private S3Client s3Client;
    
    @Autowired(required = false)
    private S3Presigner s3Presigner;
    
    private Path localStorageDir;
    private boolean isFallback = true;
    
    @PostConstruct
    public void init() {
        if (s3Client != null && s3Presigner != null && bucketName != null && !bucketName.trim().isEmpty()) {
            isFallback = false;
            logger.info("🟢 S3Service inicializado en modo AWS S3 activo (Bucket: {})", bucketName);
        } else {
            isFallback = true;
            localStorageDir = Paths.get("uploads").toAbsolutePath().normalize();
            try {
                Files.createDirectories(localStorageDir);
                logger.warn("⚠️ AWS Credentials or bucketName not configured. S3Service running in LOCAL FALLBACK mode. Local storage: {}", localStorageDir);
            } catch (IOException e) {
                logger.error("❌ No se pudo crear el directorio de almacenamiento local: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Sube un archivo a S3 o localmente si está en modo fallback
     */
    public Map<String, String> uploadFile(String s3Key, InputStream inputStream, String contentType) {
        Map<String, String> result = new HashMap<>();
        if (isFallback) {
            try {
                Path targetPath = localStorageDir.resolve(s3Key).normalize();
                // Validar contra Path Traversal
                if (!targetPath.startsWith(localStorageDir)) {
                    throw new SecurityException("Intento de Path Traversal detectado: " + s3Key);
                }
                Files.createDirectories(targetPath.getParent());
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                String downloadUrl = "/api/documentos/download-local/" + s3Key;
                result.put("key", s3Key);
                result.put("url", downloadUrl);
                logger.info("📂 Archivo subido localmente (fallback): {} -> {}", s3Key, downloadUrl);
            } catch (IOException e) {
                logger.error("❌ Error al guardar archivo local: {}", e.getMessage(), e);
                throw new RuntimeException("Error saving file locally: " + e.getMessage(), e);
            }
        } else {
            try {
                byte[] bytes = inputStream.readAllBytes();
                PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();
                
                s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));
                String downloadUrl = generatePresignedUrl(s3Key, 60);
                
                result.put("key", s3Key);
                result.put("url", downloadUrl);
                logger.info("🟢 Archivo subido a AWS S3: {}", s3Key);
            } catch (Exception e) {
                logger.error("❌ Error al subir archivo a AWS S3: {}", e.getMessage(), e);
                throw new RuntimeException("Error uploading file to S3: " + e.getMessage(), e);
            }
        }
        return result;
    }
    
    /**
     * Descarga los bytes del archivo desde S3 o local
     */
    public byte[] downloadFile(String s3Key) {
        if (isFallback) {
            try {
                Path targetPath = localStorageDir.resolve(s3Key).normalize();
                if (!targetPath.startsWith(localStorageDir)) {
                    throw new SecurityException("Intento de Path Traversal detectado: " + s3Key);
                }
                if (!Files.exists(targetPath)) {
                    throw new RuntimeException("File not found: " + s3Key);
                }
                return Files.readAllBytes(targetPath);
            } catch (IOException e) {
                logger.error("❌ Error al leer archivo local: {}", e.getMessage(), e);
                throw new RuntimeException("Error reading file locally: " + e.getMessage(), e);
            }
        } else {
            try {
                GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
                ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(getRequest);
                return responseBytes.asByteArray();
            } catch (Exception e) {
                logger.error("❌ Error al descargar archivo de AWS S3: {}", e.getMessage(), e);
                throw new RuntimeException("Error downloading file from S3: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Elimina el archivo de S3 o local
     */
    public void deleteFile(String s3Key) {
        if (isFallback) {
            try {
                Path targetPath = localStorageDir.resolve(s3Key).normalize();
                if (!targetPath.startsWith(localStorageDir)) {
                    throw new SecurityException("Intento de Path Traversal detectado: " + s3Key);
                }
                Files.deleteIfExists(targetPath);
                logger.info("🗑️ Archivo local eliminado (fallback): {}", s3Key);
            } catch (IOException e) {
                logger.warn("⚠️ No se pudo eliminar el archivo local: {}", e.getMessage());
            }
        } else {
            try {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
                s3Client.deleteObject(deleteRequest);
                logger.info("🟢 Archivo eliminado de AWS S3: {}", s3Key);
            } catch (Exception e) {
                logger.error("❌ Error al eliminar archivo de AWS S3: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Genera una URL prefirmada
     */
    public String generatePresignedUrl(String s3Key, int durationMinutes) {
        if (isFallback) {
            return "/api/documentos/download-local/" + s3Key;
        } else {
            try {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
                
                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(durationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();
                
                PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
                return presignedRequest.url().toString();
            } catch (Exception e) {
                logger.error("❌ Error generando url presignada: {}", e.getMessage(), e);
                return "/api/documentos/download-local/" + s3Key;
            }
        }
    }
}