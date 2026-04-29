package com.workflow.backend.config;

import com.workflow.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/api/kpis/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("=== CONFIGURANDO SECURITY FILTER CHAIN ===");
        
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/auth/**").permitAll()
                        // Endpoints de trámites
                        .requestMatchers("/api/tramites/**").hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                        // Endpoints de notificaciones
                        .requestMatchers("/api/notificaciones/**").hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                        // Endpoints de politicas
                        .requestMatchers(HttpMethod.POST, "/api/politicas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/politicas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/politicas/**").hasRole("ADMIN")
                        .requestMatchers("/api/politicas/**").hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                        // Endpoints de departamentos
                        .requestMatchers("/api/departamentos/**").hasAnyRole("ADMIN", "FUNCIONARIO")
                        // Endpoints de usuarios (solo ADMIN)
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // Endpoints de formularios
                        .requestMatchers("/api/formularios/**").hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                        // Cualquier otra cosa requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Usar allowedOriginPatterns en lugar de allowedOrigins para permitir credenciales
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}