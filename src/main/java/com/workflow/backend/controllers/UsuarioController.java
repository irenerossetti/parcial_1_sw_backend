package com.workflow.backend.controllers;

import com.workflow.backend.models.Usuario;
import com.workflow.backend.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // GET /api/usuarios
    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    // GET /api/usuarios/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable String id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        return usuario.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // GET /api/usuarios/email/{email}
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> getUsuarioByEmail(@PathVariable String email) {
        Optional<Usuario> usuario = usuarioService.getUsuarioByEmail(email);
        return usuario.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // POST /api/usuarios
    @PostMapping
    public ResponseEntity<Usuario> createUsuario(@RequestBody Usuario usuario) {
        Usuario nuevo = usuarioService.createUsuario(usuario);
        return ResponseEntity.ok(nuevo);
    }

    // PUT /api/usuarios/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable String id, @RequestBody Usuario usuario) {
        Usuario actualizado = usuarioService.updateUsuario(id, usuario);
        if (actualizado != null) {
            return ResponseEntity.ok(actualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // DELETE /api/usuarios/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable String id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }
}