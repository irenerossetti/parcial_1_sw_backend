package com.workflow.backend.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Data                          // Lombok: genera getters, setters, toString
@NoArgsConstructor             // Lombok: constructor vacío
@AllArgsConstructor            // Lombok: constructor con todos los campos
@Document(collection = "usuarios")  // Nombre de la colección en MongoDB
public class Usuario {

    @Id
    private String id;

    private String nombre;
    private String apellido;

    @Indexed(unique = true)    // No puede haber dos usuarios con el mismo email
    private String email;

    private String password;

    private Rol rol;           // ADMIN, FUNCIONARIO, CLIENTE

    private String departamentoId;  // A qué departamento pertenece (si es funcionario)

    private boolean activo = true;

    private LocalDateTime creadoEn = LocalDateTime.now();

    // Enum dentro de la misma clase para los roles
    public enum Rol {
        ADMIN,
        FUNCIONARIO,
        CLIENTE
    }
}