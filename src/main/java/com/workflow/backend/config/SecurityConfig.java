package com.workflow.backend.config;

import com.workflow.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        // Endpoints de trámites
                        .requestMatchers("/api/tramites").hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                        .requestMatchers("/api/tramites/mis-tramites").hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                        .requestMatchers("/api/tramites/*/estado-completo").hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers("/api/tramites/*/avanzar").hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers("/api/tramites/*/rechazar").hasAnyRole("ADMIN", "FUNCIONARIO")
                        // Endpoints de notificaciones
                        .requestMatchers("/api/notificaciones/**").hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                        // Endpoints de politicas
                        .requestMatchers("/api/politicas").hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/politicas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/politicas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/politicas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/politicas/**").hasAnyRole("ADMIN", "FUNCIONARIO", "CLIENTE")
                        // Endpoints de departamentos
                        .requestMatchers("/api/departamentos").hasAnyRole("ADMIN", "FUNCIONARIO")
                        // Endpoints de usuarios (solo ADMIN)
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // Cualquier otra cosa requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:4201", "http://localhost:5000", "http://localhost:61647"));
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