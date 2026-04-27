package com.workflow.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Activa un message broker simple en memoria
        // Los mensajes enviados a "/topic/**" serán propagados a los subscriptores
        config.enableSimpleBroker("/topic");
        
        // Prefijo para los destinos de cliente a servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket
        // Los clientes se conectarán a: ws://localhost:8080/ws-tramites
        registry.addEndpoint("/ws-tramites")
                .setAllowedOrigins("*")
                .withSockJS(); // Fallback para navegadores que no soporten WebSocket
    }
}
