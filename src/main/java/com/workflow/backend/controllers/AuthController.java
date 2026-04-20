package com.workflow.backend.controllers;

import com.workflow.backend.config.JwtUtil;
import com.workflow.backend.models.Usuario;
import com.workflow.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        return usuarioRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .map(u -> {
                    String token = jwtUtil.generarToken(
                            u.getEmail(),
                            u.getRol().name(),
                            u.getId()
                    );
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "rol", u.getRol(),
                            "nombre", u.getNombre(),
                            "userId", u.getId()
                    ));
                })
                .orElse(ResponseEntity.status(401)
                        .body(Map.of("error", "Email o contraseña incorrectos")));
    }

    // POST /api/auth/registro
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ya existe un usuario con ese email"));
        }

        // Encriptar la contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Si no se especifica rol, es CLIENTE por defecto
        if (usuario.getRol() == null) {
            usuario.setRol(Usuario.Rol.CLIENTE);
        }

        Usuario guardado = usuarioRepository.save(usuario);
        guardado.setPassword(null); // No devolver la contraseña

        return ResponseEntity.ok(guardado);
    }
}