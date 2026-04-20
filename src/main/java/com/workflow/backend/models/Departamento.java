package com.workflow.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "departamentos")
public class Departamento {

    @Id
    private String id;

    private String nombre;           // Ej: "Servicio al Cliente", "Legal"
    private String descripcion;

    private String responsableId;    // ID del Usuario jefe del departamento

    private List<String> miembrosIds; // IDs de los funcionarios en este dpto

    private boolean activo = true;

    private LocalDateTime creadoEn = LocalDateTime.now();
}