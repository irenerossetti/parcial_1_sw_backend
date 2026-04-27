package com.workflow.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Representa un campo dinámico en un formulario de trámite
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampoFormulario {

    private String nombre;              // ID único del campo (ej: "cedula_cliente")
    private String etiqueta;            // Nombre mostrado (ej: "Cédula del Cliente")
    private TipoCampo tipo;             // Tipo de input (TEXT, EMAIL, etc)
    private boolean requerido;          // Es obligatorio
    private String validacion;          // Regex para validar
    private List<OpcionCampo> opciones; // Para SELECT, RADIO, CHECKBOX
    private String ayuda;               // Texto de ayuda
    private int orden;                  // Orden de aparición

    public enum TipoCampo {
        TEXT,
        EMAIL,
        PHONE,
        DATE,
        NUMBER,
        TEXTAREA,
        SELECT,
        CHECKBOX,
        RADIO
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpcionCampo {
        private String valor;
        private String etiqueta;
    }
}
