package com.workflow.backend.controllers;

import com.workflow.backend.models.Tramite;
import com.workflow.backend.repositories.TramiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kpis")
public class KPIController {

    @Autowired
    private TramiteRepository tramiteRepository;

    /**
     * Endpoint de prueba para verificar que el backend está actualizado
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "KPI Controller está funcionando - VERSION 2.0");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para verificar autenticación y roles del usuario actual
     */
    @GetMapping("/verificar-auth")
    public ResponseEntity<?> verificarAuth() {
        Map<String, Object> response = new HashMap<>();
        
        // Obtener el usuario autenticado
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("autenticado", true);
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities().toString());
            response.put("principal", authentication.getPrincipal().toString());
        } else {
            response.put("autenticado", false);
            response.put("mensaje", "No hay usuario autenticado");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener resumen general de KPIs
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> getResumenKPIs() {
        List<Tramite> tramites = tramiteRepository.findAll();
        
        Map<String, Object> resumen = new HashMap<>();
        
        // Totales por estado
        resumen.put("total", tramites.size());
        resumen.put("nuevos", tramites.stream().filter(t -> "NUEVO".equals(t.getEstado())).count());
        resumen.put("enProceso", tramites.stream().filter(t -> "EN_PROCESO".equals(t.getEstado())).count());
        resumen.put("completados", tramites.stream().filter(t -> "COMPLETADO".equals(t.getEstado())).count());
        resumen.put("rechazados", tramites.stream().filter(t -> "RECHAZADO".equals(t.getEstado())).count());
        
        // Tasa de éxito
        long finalizados = tramites.stream()
                .filter(t -> "COMPLETADO".equals(t.getEstado()) || "RECHAZADO".equals(t.getEstado()))
                .count();
        long exitosos = tramites.stream().filter(t -> "COMPLETADO".equals(t.getEstado())).count();
        double tasaExito = finalizados > 0 ? (exitosos * 100.0 / finalizados) : 0;
        resumen.put("tasaExito", Math.round(tasaExito * 100.0) / 100.0);
        
        // Tiempo promedio de completado
        List<Tramite> completados = tramites.stream()
                .filter(t -> "COMPLETADO".equals(t.getEstado()) && t.getFinalizadoEn() != null)
                .collect(Collectors.toList());
        
        if (!completados.isEmpty()) {
            double promedioHoras = completados.stream()
                    .mapToLong(t -> ChronoUnit.HOURS.between(t.getCreadoEn(), t.getFinalizadoEn()))
                    .average()
                    .orElse(0);
            resumen.put("tiempoPromedioHoras", Math.round(promedioHoras * 100.0) / 100.0);
        } else {
            resumen.put("tiempoPromedioHoras", 0);
        }
        
        return ResponseEntity.ok(resumen);
    }

    /**
     * Obtener trámites por estado (para gráfico de pastel)
     */
    @GetMapping("/por-estado")
    public ResponseEntity<?> getTramitesPorEstado() {
        List<Tramite> tramites = tramiteRepository.findAll();
        
        Map<String, Long> porEstado = tramites.stream()
                .collect(Collectors.groupingBy(
                    t -> t.getEstado().toString(), 
                    Collectors.counting()
                ));
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        porEstado.forEach((estado, cantidad) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("estado", estado);
            item.put("cantidad", cantidad);
            resultado.add(item);
        });
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtener trámites por departamento
     */
    @GetMapping("/por-departamento")
    public ResponseEntity<?> getTramitesPorDepartamento() {
        List<Tramite> tramites = tramiteRepository.findAll();
        
        Map<String, Long> porDepartamento = tramites.stream()
                .filter(t -> t.getDepartamentoActual() != null && !t.getDepartamentoActual().isEmpty())
                .collect(Collectors.groupingBy(Tramite::getDepartamentoActual, Collectors.counting()));
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        porDepartamento.forEach((depto, cantidad) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("departamento", depto);
            item.put("cantidad", cantidad);
            resultado.add(item);
        });
        
        // Ordenar por cantidad descendente
        resultado.sort((a, b) -> Long.compare((Long)b.get("cantidad"), (Long)a.get("cantidad")));
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtener tendencia temporal (últimos 30 días)
     */
    @GetMapping("/tendencia")
    public ResponseEntity<?> getTendenciaTemporal(@RequestParam(defaultValue = "30") int dias) {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(dias);
        List<Tramite> tramites = tramiteRepository.findAll().stream()
                .filter(t -> t.getCreadoEn().isAfter(fechaInicio))
                .collect(Collectors.toList());
        
        // Agrupar por día
        Map<String, Long> porDia = new TreeMap<>();
        for (int i = 0; i < dias; i++) {
            LocalDateTime fecha = LocalDateTime.now().minusDays(dias - i - 1);
            String fechaStr = fecha.toLocalDate().toString();
            porDia.put(fechaStr, 0L);
        }
        
        tramites.forEach(t -> {
            String fechaStr = t.getCreadoEn().toLocalDate().toString();
            porDia.merge(fechaStr, 1L, Long::sum);
        });
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        porDia.forEach((fecha, cantidad) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("fecha", fecha);
            item.put("cantidad", cantidad);
            resultado.add(item);
        });
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtener rendimiento por departamento
     */
    @GetMapping("/rendimiento-departamentos")
    public ResponseEntity<?> getRendimientoDepartamentos() {
        List<Tramite> tramites = tramiteRepository.findAll();
        
        Map<String, List<Tramite>> porDepartamento = tramites.stream()
                .filter(t -> t.getDepartamentoActual() != null && !t.getDepartamentoActual().isEmpty())
                .collect(Collectors.groupingBy(Tramite::getDepartamentoActual));
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        porDepartamento.forEach((depto, tramitesDepto) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("departamento", depto);
            item.put("total", tramitesDepto.size());
            
            long completados = tramitesDepto.stream()
                    .filter(t -> "COMPLETADO".equals(t.getEstado()))
                    .count();
            item.put("completados", completados);
            
            long enProceso = tramitesDepto.stream()
                    .filter(t -> "EN_PROCESO".equals(t.getEstado()))
                    .count();
            item.put("enProceso", enProceso);
            
            // Tiempo promedio
            List<Tramite> completadosConTiempo = tramitesDepto.stream()
                    .filter(t -> "COMPLETADO".equals(t.getEstado()) && t.getFinalizadoEn() != null)
                    .collect(Collectors.toList());
            
            if (!completadosConTiempo.isEmpty()) {
                double promedioHoras = completadosConTiempo.stream()
                        .mapToLong(t -> ChronoUnit.HOURS.between(t.getCreadoEn(), t.getFinalizadoEn()))
                        .average()
                        .orElse(0);
                item.put("tiempoPromedioHoras", Math.round(promedioHoras * 100.0) / 100.0);
            } else {
                item.put("tiempoPromedioHoras", 0);
            }
            
            // Eficiencia (% completados)
            double eficiencia = tramitesDepto.size() > 0 
                    ? (completados * 100.0 / tramitesDepto.size()) 
                    : 0;
            item.put("eficiencia", Math.round(eficiencia * 100.0) / 100.0);
            
            resultado.add(item);
        });
        
        // Ordenar por eficiencia descendente
        resultado.sort((a, b) -> Double.compare((Double)b.get("eficiencia"), (Double)a.get("eficiencia")));
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtener comparativa mensual
     */
    @GetMapping("/comparativa-mensual")
    public ResponseEntity<?> getComparativaMensual() {
        List<Tramite> tramites = tramiteRepository.findAll();
        LocalDateTime hace6Meses = LocalDateTime.now().minusMonths(6);
        
        tramites = tramites.stream()
                .filter(t -> t.getCreadoEn().isAfter(hace6Meses))
                .collect(Collectors.toList());
        
        Map<String, Map<String, Long>> porMesYEstado = new TreeMap<>();
        
        tramites.forEach(t -> {
            String mes = t.getCreadoEn().getYear() + "-" + 
                        String.format("%02d", t.getCreadoEn().getMonthValue());
            
            porMesYEstado.putIfAbsent(mes, new HashMap<>());
            Map<String, Long> estadosMes = porMesYEstado.get(mes);
            estadosMes.merge(t.getEstado().toString(), 1L, Long::sum);
        });
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        porMesYEstado.forEach((mes, estados) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("mes", mes);
            item.put("completados", estados.getOrDefault("COMPLETADO", 0L));
            item.put("rechazados", estados.getOrDefault("RECHAZADO", 0L));
            item.put("enProceso", estados.getOrDefault("EN_PROCESO", 0L));
            item.put("total", estados.values().stream().mapToLong(Long::longValue).sum());
            resultado.add(item);
        });
        
        return ResponseEntity.ok(resultado);
    }
}
