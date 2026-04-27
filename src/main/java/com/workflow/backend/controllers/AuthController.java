package com.workflow.backend.controllers;

import com.workflow.backend.models.Usuario;
import com.workflow.backend.repositories.UsuarioRepository;
import com.workflow.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());

            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", usuario != null && usuario.getId() != null ? usuario.getId() : email);
            response.put("email", email);
            response.put("rol", usuario != null ? usuario.getRol() : "CLIENTE");
            response.put("nombre", usuario != null ? usuario.getNombre() : "");
            response.put("departamentoNombre", usuario != null ? usuario.getDepartamentoNombre() : "");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        String email = userData.get("email");

        if (usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("El usuario ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(userData.get("password")));
        usuario.setNombre(userData.get("nombre"));
        usuario.setRol("CLIENTE");
        usuario.setActivo(true);

        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Usuario registrado exitosamente");
    }
}