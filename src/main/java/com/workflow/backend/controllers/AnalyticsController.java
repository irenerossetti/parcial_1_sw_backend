package com.workflow.backend.controllers;

import com.workflow.backend.dto.CuelloBottellaResponse;
import com.workflow.backend.dto.CuelloBottellaResponse.DepartamentoAnalytics;
import com.workflow.backend.dto.TramiteAtrasadoDTO;
import com.workflow.backend.models.Tramite;
import com.workflow.backend.models.Tramite.EstadoTramite;
import com.workflow.backend.repositories.TramiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201", "http://localhost:5000", "http://localhost:61647"})
public class AnalyticsController {

    @Autowired
    private TramiteRepository tramiteRepository;

    /**
     * Obtiene análisis de cuellos de botella por departamento
     * Roles: ADMIN, FUNCIONARIO
     */
    @GetMapping("/cuellos-botella")
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    public ResponseEntity<CuelloBottellaResponse> obtenerCuellosBottella() {
        try {
            // Obtener todos los trámites en proceso y nuevos
            List<Tramite> tramitesEnEspera = tramiteRepository.findAll().stream()
                    .filter(t -> t.getEstado() == EstadoTramite.EN_PROCESO || 
                                 t.getEstado() == EstadoTramite.NUEVO)
                    .collect(Collectors.toList());

            // Agrupar por departamento
            Map<String, List<Tramite>> tramitesPorDepto = tramitesEnEspera.stream()
                    .collect(Collectors.groupingBy(
                            t -> t.getDepartamentoActual() != null ? t.getDepartamentoActual() : "Sin asignar",
                            Collectors.toList()
                    ));

            // Calcular métricas por departamento
            List<DepartamentoAnalytics> departamentosAnalytics = new ArrayList<>();
            int totalTramites = 0;
            String departamentoCritico = null;
            int maxTramites = 0;

            for (Map.Entry<String, List<Tramite>> entry : tramitesPorDepto.entrySet()) {
                String nombreDepto = entry.getKey();
                List<Tramite> tramites = entry.getValue();

                int cantidadTramites = tramites.size();
                totalTramites += cantidadTramites;

                // Calcular tiempo promedio de espera (en horas)
                double tiempoPromedioHoras = calcularTiempoPromedioHoras(tramites);

                // Calcular capacidad utilizada (asumiendo capacidad máxima de 10 por departamento)
                int capacidadMaxima = 10;
                int capacidadUtilizada = Math.min(100, (cantidadTramites * 100) / capacidadMaxima);

                // Determinar estado
                String estado = determinarEstado(cantidadTramites, tiempoPromedioHoras);

                departamentosAnalytics.add(new DepartamentoAnalytics(
                        nombreDepto,
                        cantidadTramites,
                        tiempoPromedioHoras,
                        capacidadUtilizada,
                        estado
                ));

                // Identificar departamento más crítico
                if (cantidadTramites > maxTramites) {
                    maxTramites = cantidadTramites;
                    departamentoCritico = nombreDepto;
                }
            }

            // Obtener trámites atrasados (> 3 días)
            List<TramiteAtrasadoDTO> tramitesAtrasados = obtenerTramitesAtrasados(tramitesEnEspera);

            // Ordenar departamentos por número de trámites descendente
            departamentosAnalytics.sort((a, b) -> Integer.compare(b.getTramitesEnEspera(), a.getTramitesEnEspera()));

            CuelloBottellaResponse respuesta = new CuelloBottellaResponse(
                    departamentosAnalytics,
                    tramitesAtrasados,
                    totalTramites,
                    departamentoCritico
            );

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Calcula el tiempo promedio de espera en horas para un conjunto de trámites
     */
    private double calcularTiempoPromedioHoras(List<Tramite> tramites) {
        if (tramites.isEmpty()) {
            return 0;
        }

        double totalHoras = 0;
        LocalDateTime ahora = LocalDateTime.now();

        for (Tramite tramite : tramites) {
            if (tramite.getCreadoEn() != null) {
                long horas = ChronoUnit.HOURS.between(tramite.getCreadoEn(), ahora);
                totalHoras += horas;
            }
        }

        return Math.round((totalHoras / tramites.size()) * 10.0) / 10.0;
    }

    /**
     * Determina el estado del departamento basado en cantidad y tiempo de espera
     */
    private String determinarEstado(int cantidadTramites, double tiempoPromedioHoras) {
        // CRÍTICO: > 8 trámites O > 72 horas (3 días)
        if (cantidadTramites > 8 || tiempoPromedioHoras > 72) {
            return "CRITICO";
        }
        // ADVERTENCIA: > 5 trámites O > 48 horas
        else if (cantidadTramites > 5 || tiempoPromedioHoras > 48) {
            return "ADVERTENCIA";
        }
        // NORMAL
        else {
            return "NORMAL";
        }
    }

    /**
     * Obtiene trámites que llevan más de 3 días en espera
     */
    private List<TramiteAtrasadoDTO> obtenerTramitesAtrasados(List<Tramite> tramites) {
        LocalDateTime hace3Dias = LocalDateTime.now().minusDays(3);

        return tramites.stream()
                .filter(t -> t.getCreadoEn() != null && t.getCreadoEn().isBefore(hace3Dias))
                .map(t -> {
                    long demoraDias = ChronoUnit.DAYS.between(t.getCreadoEn(), LocalDateTime.now());
                    return new TramiteAtrasadoDTO(
                            t.getCodigo(),
                            t.getClienteNombre() != null ? t.getClienteNombre() : t.getClienteEmail(),
                            demoraDias,
                            t.getDepartamentoActual()
                    );
                })
                .sorted((a, b) -> Long.compare(b.getDemoraDias(), a.getDemoraDias()))
                .collect(Collectors.toList());
    }
}
