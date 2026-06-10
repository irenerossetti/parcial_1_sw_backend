package com.workflow.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuración de AWS S3
 * Crea bean S3Client y S3Presigner para inyección de dependencias si las credenciales están presentes
 */
@Configuration
public class AmazonS3Config {
    
    private static final Logger logger = LoggerFactory.getLogger(AmazonS3Config.class);
    
    @Value("${aws.accessKeyId:}")
    private String accessKeyId;
    
    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;
    
    @Value("${aws.s3.region:us-east-1}")
    private String awsRegion;
    
    @Value("${aws.s3.bucketName:}")
    private String bucketName;
    
    /**
     * Bean para S3Client
     */
    @Bean
    public S3Client s3Client() {
        if (accessKeyId == null || accessKeyId.trim().isEmpty() ||
            secretAccessKey == null || secretAccessKey.trim().isEmpty() ||
            bucketName == null || bucketName.trim().isEmpty()) {
            logger.warn("⚠️ AWS Credentials or bucketName not configured. S3Client bean will NOT be initialized.");
            return null;
        }
        
        try {
            logger.info("Inicializando S3Client para región: {}", awsRegion);
            logger.info("Bucket: {}", bucketName);
            
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                accessKeyId,
                secretAccessKey
            );
            
            return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(awsRegion))
                .serviceConfiguration(S3Configuration.builder()
                    .checksumValidationEnabled(true)
                    .build())
                .build();
        } catch (Exception e) {
            logger.error("❌ Error creando S3Client: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Bean para S3Presigner
     */
    @Bean
    public S3Presigner s3Presigner() {
        if (accessKeyId == null || accessKeyId.trim().isEmpty() ||
            secretAccessKey == null || secretAccessKey.trim().isEmpty()) {
            logger.warn("⚠️ AWS Credentials not configured. S3Presigner bean will NOT be initialized.");
            return null;
        }
        
        try {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                accessKeyId,
                secretAccessKey
            );
            
            return S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(awsRegion))
                .build();
        } catch (Exception e) {
            logger.error("❌ Error creando S3Presigner: {}", e.getMessage(), e);
            return null;
        }
    }
}