package com.workflow.backend.config;

import com.workflow.backend.models.Departamento;
import com.workflow.backend.models.Usuario;
import com.workflow.backend.repositories.DepartamentoRepository;
import com.workflow.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== CARGANDO DATOS INICIALES ===");

        // Crear departamentos
        if (departamentoRepository.count() == 0) {
            System.out.println("Creando departamentos...");

            Departamento d1 = new Departamento();
            d1.setNombre("Atención al Cliente");
            d1.setCodigo("DPT-001");
            d1.setEstado(Departamento.EstadoDepartamento.ACTIVO);
            departamentoRepository.save(d1);

            Departamento d2 = new Departamento();
            d2.setNombre("Evaluación");
            d2.setCodigo("DPT-002");
            d2.setEstado(Departamento.EstadoDepartamento.ACTIVO);
            departamentoRepository.save(d2);

            Departamento d3 = new Departamento();
            d3.setNombre("Legal");
            d3.setCodigo("DPT-003");
            d3.setEstado(Departamento.EstadoDepartamento.ACTIVO);
            departamentoRepository.save(d3);

            Departamento d4 = new Departamento();
            d4.setNombre("Almacén");
            d4.setCodigo("DPT-004");
            d4.setEstado(Departamento.EstadoDepartamento.ACTIVO);
            departamentoRepository.save(d4);

            System.out.println("Departamentos creados: " + departamentoRepository.count());
        }

        // Crear usuarios
        if (usuarioRepository.count() == 0) {
            System.out.println("Creando usuarios...");

            Usuario admin = new Usuario();
            admin.setEmail("admin@workflow.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador");
            admin.setRol("ADMIN");
            admin.setActivo(true);
            usuarioRepository.save(admin);

            Usuario funcionario1 = new Usuario();
            funcionario1.setEmail("funcionario@workflow.com");
            funcionario1.setPassword(passwordEncoder.encode("funcionario123"));
            funcionario1.setNombre("Funcionario - Atención al Cliente");
            funcionario1.setRol("FUNCIONARIO");
            funcionario1.setDepartamentoNombre("Atención al Cliente");
            funcionario1.setActivo(true);
            usuarioRepository.save(funcionario1);

            Usuario funcionario2 = new Usuario();
            funcionario2.setEmail("funcionario2@workflow.com");
            funcionario2.setPassword(passwordEncoder.encode("funcionario123"));
            funcionario2.setNombre("Funcionario - Evaluación");
            funcionario2.setRol("FUNCIONARIO");
            funcionario2.setDepartamentoNombre("Evaluación");
            funcionario2.setActivo(true);
            usuarioRepository.save(funcionario2);

            Usuario funcionario3 = new Usuario();
            funcionario3.setEmail("funcionario3@workflow.com");
            funcionario3.setPassword(passwordEncoder.encode("funcionario123"));
            funcionario3.setNombre("Funcionario - Legal");
            funcionario3.setRol("FUNCIONARIO");
            funcionario3.setDepartamentoNombre("Legal");
            funcionario3.setActivo(true);
            usuarioRepository.save(funcionario3);

            Usuario cliente = new Usuario();
            cliente.setEmail("cliente@workflow.com");
            cliente.setPassword(passwordEncoder.encode("cliente123"));
            cliente.setNombre("Cliente");
            cliente.setRol("CLIENTE");
            cliente.setActivo(true);
            usuarioRepository.save(cliente);

            System.out.println("Usuarios creados: " + usuarioRepository.count());
        }

        System.out.println("=== DATOS CARGADOS ===");
    }
}
