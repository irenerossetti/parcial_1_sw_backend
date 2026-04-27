package com.workflow.backend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "departamentos")
public class Departamento {

    @Id
    private String id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String responsableEmail;
    private String responsableNombre;
    private TipoDepartamento tipo;
    private EstadoDepartamento estado;
    private List<String> rolesPermitidos;
    private ConfiguracionFlujo configuracionFlujo;
    private MetricasDesempenio metricas;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    // Enums
    public enum TipoDepartamento {
        ATENCION_CLIENTE,
        EVALUACION,
        LEGAL,
        TECNICO,
        ALMACEN,
        FINANZAS,
        CALIDAD,
        SOPORTE,
        OTROS
    }

    public enum EstadoDepartamento {
        ACTIVO,
        INACTIVO,
        MANTENIMIENTO
    }

    // Clase interna para configuración de flujo
    public static class ConfiguracionFlujo {
        private int tiempoMaximoHoras;
        private boolean requiereAprobacion;
        private List<String> departamentosSiguientes;
        private List<String> accionesPermitidas;
        private PlantillaFormulario formulario;

        // Getters y Setters
        public int getTiempoMaximoHoras() { return tiempoMaximoHoras; }
        public void setTiempoMaximoHoras(int tiempoMaximoHoras) { this.tiempoMaximoHoras = tiempoMaximoHoras; }

        public boolean isRequiereAprobacion() { return requiereAprobacion; }
        public void setRequiereAprobacion(boolean requiereAprobacion) { this.requiereAprobacion = requiereAprobacion; }

        public List<String> getDepartamentosSiguientes() { return departamentosSiguientes; }
        public void setDepartamentosSiguientes(List<String> departamentosSiguientes) { this.departamentosSiguientes = departamentosSiguientes; }

        public List<String> getAccionesPermitidas() { return accionesPermitidas; }
        public void setAccionesPermitidas(List<String> accionesPermitidas) { this.accionesPermitidas = accionesPermitidas; }

        public PlantillaFormulario getFormulario() { return formulario; }
        public void setFormulario(PlantillaFormulario formulario) { this.formulario = formulario; }
    }

    // Clase interna para plantilla de formulario
    public static class PlantillaFormulario {
        private String titulo;
        private List<CampoFormulario> campos;
        private String instrucciones;

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public List<CampoFormulario> getCampos() { return campos; }
        public void setCampos(List<CampoFormulario> campos) { this.campos = campos; }

        public String getInstrucciones() { return instrucciones; }
        public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
    }

    // Clase interna para campos de formulario
    public static class CampoFormulario {
        private String nombre;
        private String etiqueta;
        private TipoCampo tipo;
        private boolean requerido;
        private List<String> opciones;
        private String valorPorDefecto;

        public enum TipoCampo {
            TEXTO, TEXTAREA, NUMERO, FECHA, SELECT, CHECKBOX, RADIO, ARCHIVO
        }

        // Getters y Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getEtiqueta() { return etiqueta; }
        public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

        public TipoCampo getTipo() { return tipo; }
        public void setTipo(TipoCampo tipo) { this.tipo = tipo; }

        public boolean isRequerido() { return requerido; }
        public void setRequerido(boolean requerido) { this.requerido = requerido; }

        public List<String> getOpciones() { return opciones; }
        public void setOpciones(List<String> opciones) { this.opciones = opciones; }

        public String getValorPorDefecto() { return valorPorDefecto; }
        public void setValorPorDefecto(String valorPorDefecto) { this.valorPorDefecto = valorPorDefecto; }
    }

    // Clase interna para métricas de desempeño
    public static class MetricasDesempenio {
        private int tramitesAtendidos;
        private int tramitesPendientes;
        private int tramitesEnMora;
        private double tiempoPromedioProcesamiento;
        private double eficiencia;
        private LocalDateTime ultimaActualizacion;

        // Getters y Setters
        public int getTramitesAtendidos() { return tramitesAtendidos; }
        public void setTramitesAtendidos(int tramitesAtendidos) { this.tramitesAtendidos = tramitesAtendidos; }

        public int getTramitesPendientes() { return tramitesPendientes; }
        public void setTramitesPendientes(int tramitesPendientes) { this.tramitesPendientes = tramitesPendientes; }

        public int getTramitesEnMora() { return tramitesEnMora; }
        public void setTramitesEnMora(int tramitesEnMora) { this.tramitesEnMora = tramitesEnMora; }

        public double getTiempoPromedioProcesamiento() { return tiempoPromedioProcesamiento; }
        public void setTiempoPromedioProcesamiento(double tiempoPromedioProcesamiento) { this.tiempoPromedioProcesamiento = tiempoPromedioProcesamiento; }

        public double getEficiencia() { return eficiencia; }
        public void setEficiencia(double eficiencia) { this.eficiencia = eficiencia; }

        public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }
        public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
    }

    // Constructores
    public Departamento() {
        this.rolesPermitidos = new ArrayList<>();
        this.configuracionFlujo = new ConfiguracionFlujo();
        this.metricas = new MetricasDesempenio();
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getResponsableEmail() { return responsableEmail; }
    public void setResponsableEmail(String responsableEmail) { this.responsableEmail = responsableEmail; }

    public String getResponsableNombre() { return responsableNombre; }
    public void setResponsableNombre(String responsableNombre) { this.responsableNombre = responsableNombre; }

    public TipoDepartamento getTipo() { return tipo; }
    public void setTipo(TipoDepartamento tipo) { this.tipo = tipo; }

    public EstadoDepartamento getEstado() { return estado; }
    public void setEstado(EstadoDepartamento estado) { this.estado = estado; }

    public List<String> getRolesPermitidos() { return rolesPermitidos; }
    public void setRolesPermitidos(List<String> rolesPermitidos) { this.rolesPermitidos = rolesPermitidos; }

    public ConfiguracionFlujo getConfiguracionFlujo() { return configuracionFlujo; }
    public void setConfiguracionFlujo(ConfiguracionFlujo configuracionFlujo) { this.configuracionFlujo = configuracionFlujo; }

    public MetricasDesempenio getMetricas() { return metricas; }
    public void setMetricas(MetricasDesempenio metricas) { this.metricas = metricas; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}