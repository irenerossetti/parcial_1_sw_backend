package com.workflow.backend.services;

import com.workflow.backend.models.Usuario;
import com.workflow.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(String id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario createUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario updateUsuario(String id, Usuario usuarioDetails) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setNombre(usuarioDetails.getNombre());
            usuario.setEmail(usuarioDetails.getEmail());
            usuario.setRol(usuarioDetails.getRol());
            usuario.setDepartamentoNombre(usuarioDetails.getDepartamentoNombre());
            usuario.setActivo(usuarioDetails.isActivo());
            // Solo actualizar contraseña si se proporciona una nueva
            if (usuarioDetails.getPassword() != null && !usuarioDetails.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuarioDetails.getPassword()));
            }
            return usuarioRepository.save(usuario);
        }
        return null;
    }

    public void deleteUsuario(String id) {
        usuarioRepository.deleteById(id);
    }
}