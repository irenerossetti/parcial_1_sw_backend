package com.workflow.backend.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CuelloBottellaResponse {
    
    @JsonProperty("departamentos")
    private List<DepartamentoAnalytics> departamentos;
    
    @JsonProperty("tramitesAtrasados")
    private List<TramiteAtrasadoDTO> tramitesAtrasados;
    
    @JsonProperty("totalTramitesEnEspera")
    private int totalTramitesEnEspera;
    
    @JsonProperty("departamentoCritico")
    private String departamentoCritico;

    public CuelloBottellaResponse() {}

    public CuelloBottellaResponse(List<DepartamentoAnalytics> departamentos, 
                                  List<TramiteAtrasadoDTO> tramitesAtrasados,
                                  int totalTramitesEnEspera,
                                  String departamentoCritico) {
        this.departamentos = departamentos;
        this.tramitesAtrasados = tramitesAtrasados;
        this.totalTramitesEnEspera = totalTramitesEnEspera;
        this.departamentoCritico = departamentoCritico;
    }

    public List<DepartamentoAnalytics> getDepartamentos() {
        return departamentos;
    }

    public void setDepartamentos(List<DepartamentoAnalytics> departamentos) {
        this.departamentos = departamentos;
    }

    public List<TramiteAtrasadoDTO> getTramitesAtrasados() {
        return tramitesAtrasados;
    }

    public void setTramitesAtrasados(List<TramiteAtrasadoDTO> tramitesAtrasados) {
        this.tramitesAtrasados = tramitesAtrasados;
    }

    public int getTotalTramitesEnEspera() {
        return totalTramitesEnEspera;
    }

    public void setTotalTramitesEnEspera(int totalTramitesEnEspera) {
        this.totalTramitesEnEspera = totalTramitesEnEspera;
    }

    public String getDepartamentoCritico() {
        return departamentoCritico;
    }

    public void setDepartamentoCritico(String departamentoCritico) {
        this.departamentoCritico = departamentoCritico;
    }

    // Inner class for department analytics
    public static class DepartamentoAnalytics {
        @JsonProperty("nombre")
        private String nombre;
        
        @JsonProperty("tramitesEnEspera")
        private int tramitesEnEspera;
        
        @JsonProperty("tiempoPromedioHoras")
        private double tiempoPromedioHoras;
        
        @JsonProperty("capacidadUtilizada")
        private int capacidadUtilizada;
        
        @JsonProperty("estado")
        private String estado; // "CRITICO", "ADVERTENCIA", "NORMAL"

        public DepartamentoAnalytics() {}

        public DepartamentoAnalytics(String nombre, int tramitesEnEspera, 
                                     double tiempoPromedioHoras, int capacidadUtilizada, 
                                     String estado) {
            this.nombre = nombre;
            this.tramitesEnEspera = tramitesEnEspera;
            this.tiempoPromedioHoras = tiempoPromedioHoras;
            this.capacidadUtilizada = capacidadUtilizada;
            this.estado = estado;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public int getTramitesEnEspera() { return tramitesEnEspera; }
        public void setTramitesEnEspera(int tramitesEnEspera) { this.tramitesEnEspera = tramitesEnEspera; }

        public double getTiempoPromedioHoras() { return tiempoPromedioHoras; }
        public void setTiempoPromedioHoras(double tiempoPromedioHoras) { this.tiempoPromedioHoras = tiempoPromedioHoras; }

        public int getCapacidadUtilizada() { return capacidadUtilizada; }
        public void setCapacidadUtilizada(int capacidadUtilizada) { this.capacidadUtilizada = capacidadUtilizada; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}
