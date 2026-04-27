package com.workflow.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TramiteAtrasadoDTO {
    
    @JsonProperty("codigo")
    private String codigo;
    
    @JsonProperty("cliente")
    private String cliente;
    
    @JsonProperty("demoraDias")
    private long demoraDias;
    
    @JsonProperty("departamentoActual")
    private String departamentoActual;

    public TramiteAtrasadoDTO() {}

    public TramiteAtrasadoDTO(String codigo, String cliente, long demoraDias, String departamentoActual) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.demoraDias = demoraDias;
        this.departamentoActual = departamentoActual;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public long getDemoraDias() { return demoraDias; }
    public void setDemoraDias(long demoraDias) { this.demoraDias = demoraDias; }

    public String getDepartamentoActual() { return departamentoActual; }
    public void setDepartamentoActual(String departamentoActual) { this.departamentoActual = departamentoActual; }
}
