package com.workflow.backend.config;

import com.workflow.backend.models.CampoFormulario;
import com.workflow.backend.models.Departamento;
import com.workflow.backend.models.PoliticaNegocio;
import com.workflow.backend.models.Usuario;
import com.workflow.backend.repositories.DepartamentoRepository;
import com.workflow.backend.repositories.PoliticaNegocioRepository;
import com.workflow.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private PoliticaNegocioRepository politicaNegocioRepository;

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

            // Crear politica semilla si no hay politicas activas
            if (politicaNegocioRepository.countByActivoTrue() == 0) {
                System.out.println("Creando politica de negocio semilla...");

                PoliticaNegocio.Nodo inicio = new PoliticaNegocio.Nodo(
                    "NODO_INICIO",
                    "Inicio",
                    "Inicio del flujo",
                    null,
                    null,
                    PoliticaNegocio.Nodo.TipoNodo.INICIO,
                    List.of("NODO_RECEPCION"),
                    null,
                    0,
                    List.of()
                );

                PoliticaNegocio.Nodo recepcion = new PoliticaNegocio.Nodo(
                    "NODO_RECEPCION",
                    "Recepcion de solicitud",
                    "Atencion inicial del tramite",
                    "Atencion al Cliente",
                    "funcionario@workflow.com",
                    PoliticaNegocio.Nodo.TipoNodo.TAREA,
                    List.of("NODO_EVALUACION"),
                    null,
                    1,
                    List.of("tipoTramite", "descripcion")
                );

                PoliticaNegocio.Nodo evaluacion = new PoliticaNegocio.Nodo(
                    "NODO_EVALUACION",
                    "Evaluacion",
                    "Revision y evaluacion de documentos",
                    "Evaluacion",
                    null,
                    PoliticaNegocio.Nodo.TipoNodo.TAREA,
                    List.of("NODO_FIN"),
                    null,
                    2,
                    List.of("observaciones")
                );

                PoliticaNegocio.Nodo fin = new PoliticaNegocio.Nodo(
                    "NODO_FIN",
                    "Fin",
                    "Cierre del tramite",
                    null,
                    null,
                    PoliticaNegocio.Nodo.TipoNodo.FIN,
                    List.of(),
                    null,
                    3,
                    List.of()
                );

                PoliticaNegocio politica = new PoliticaNegocio();
                politica.setNombre("Flujo Basico de Tramite");
                politica.setDescripcion("Flujo lineal semilla para demostracion");
                politica.setEmpresaId("WORKFLOW-DEMO");
                politica.setTipoFlujo(PoliticaNegocio.TipoFlujo.LINEAL);
                politica.setActivo(true);
                politica.setCreadoEn(LocalDateTime.now());
                politica.setActualizadoEn(LocalDateTime.now());
                politica.setFlujo(List.of(inicio, recepcion, evaluacion, fin));
                politica.setCampos(crearCamposDinamicos());

                politicaNegocioRepository.save(politica);

                System.out.println("Politicas activas: " + politicaNegocioRepository.countByActivoTrue());
            }

            // Crear politica alternativa de demostracion (sin duplicados)
            if (!politicaNegocioRepository.existsByNombre("Flujo Alternativo Demo")) {
                System.out.println("Creando politica alternativa de demostracion...");

                PoliticaNegocio.Nodo inicioAlt = new PoliticaNegocio.Nodo(
                    "ALT_INICIO",
                    "Inicio",
                    "Inicio del flujo alternativo",
                    null,
                    null,
                    PoliticaNegocio.Nodo.TipoNodo.INICIO,
                    List.of("ALT_RECEPCION"),
                    null,
                    0,
                    List.of()
                );

                PoliticaNegocio.Nodo recepcionAlt = new PoliticaNegocio.Nodo(
                    "ALT_RECEPCION",
                    "Recepcion",
                    "Revision inicial del tramite",
                    "Atencion al Cliente",
                    "funcionario@workflow.com",
                    PoliticaNegocio.Nodo.TipoNodo.TAREA,
                    List.of("ALT_DECISION"),
                    null,
                    1,
                    List.of("tipoTramite", "descripcion")
                );

                PoliticaNegocio.Nodo decisionAlt = new PoliticaNegocio.Nodo(
                    "ALT_DECISION",
                    "Decision de ruta",
                    "Seleccion de ruta entre aprobacion rapida o revision detallada",
                    "Atencion al Cliente",
                    "funcionario@workflow.com",
                    PoliticaNegocio.Nodo.TipoNodo.DECISION,
                    List.of("ALT_APROBACION", "ALT_REVISION"),
                    "segun criterio del funcionario",
                    2,
                    List.of("criterioDecision")
                );

                PoliticaNegocio.Nodo aprobacionAlt = new PoliticaNegocio.Nodo(
                    "ALT_APROBACION",
                    "Aprobacion rapida",
                    "Ruta corta para casos simples",
                    "Atencion al Cliente",
                    "funcionario@workflow.com",
                    PoliticaNegocio.Nodo.TipoNodo.TAREA,
                    List.of("ALT_FIN"),
                    null,
                    3,
                    List.of("observaciones")
                );

                PoliticaNegocio.Nodo revisionAlt = new PoliticaNegocio.Nodo(
                    "ALT_REVISION",
                    "Revision detallada",
                    "Ruta extendida para casos especiales",
                    "Evaluacion",
                    null,
                    PoliticaNegocio.Nodo.TipoNodo.TAREA,
                    List.of("ALT_FIN"),
                    null,
                    3,
                    List.of("justificacion", "documentacion")
                );

                PoliticaNegocio.Nodo finAlt = new PoliticaNegocio.Nodo(
                    "ALT_FIN",
                    "Fin",
                    "Cierre del flujo alternativo",
                    null,
                    null,
                    PoliticaNegocio.Nodo.TipoNodo.FIN,
                    List.of(),
                    null,
                    4,
                    List.of()
                );

                PoliticaNegocio politicaAlternativa = new PoliticaNegocio();
                politicaAlternativa.setNombre("Flujo Alternativo Demo");
                politicaAlternativa.setDescripcion("Flujo de demostracion con bifurcacion controlada para pruebas");
                politicaAlternativa.setEmpresaId("WORKFLOW-DEMO");
                politicaAlternativa.setTipoFlujo(PoliticaNegocio.TipoFlujo.ALTERNATIVO);
                politicaAlternativa.setActivo(true);
                politicaAlternativa.setCreadoEn(LocalDateTime.now());
                politicaAlternativa.setActualizadoEn(LocalDateTime.now());
                politicaAlternativa.setFlujo(List.of(inicioAlt, recepcionAlt, decisionAlt, aprobacionAlt, revisionAlt, finAlt));
                politicaAlternativa.setCampos(crearCamposDinamicos());

                politicaNegocioRepository.save(politicaAlternativa);
                System.out.println("Politica alternativa creada");
            }

        // Asegurar que todas las políticas activas tengan campos dinámicos
        System.out.println("Actualizando campos dinámicos en políticas existentes...");
        List<PoliticaNegocio> politicasActivas = politicaNegocioRepository.findByActivoTrue();
        boolean camposActualizados = false;
        
        for (PoliticaNegocio politica : politicasActivas) {
            if (politica.getCampos() == null || politica.getCampos().isEmpty()) {
                System.out.println("Agregando campos a política: " + politica.getNombre());
                politica.setCampos(crearCamposDinamicos());
                politicaNegocioRepository.save(politica);
                camposActualizados = true;
            }
        }
        
        if (camposActualizados) {
            System.out.println("Campos dinámicos actualizados en políticas");
        }

        System.out.println("=== DATOS CARGADOS ===");
    }

    /**
     * Crea los campos dinámicos para los formularios de trámites
     */
    private List<CampoFormulario> crearCamposDinamicos() {
        List<CampoFormulario> campos = new ArrayList<>();

        // Cédula del Cliente
        campos.add(new CampoFormulario(
            "cedula",
            "Cédula de Identidad",
            CampoFormulario.TipoCampo.TEXT,
            true,
            "^[0-9]{6,10}$",
            null,
            "Ingrese su número de cédula sin puntos ni guiones",
            0
        ));

        // Nombre Completo
        campos.add(new CampoFormulario(
            "nombreCompleto",
            "Nombre Completo",
            CampoFormulario.TipoCampo.TEXT,
            true,
            "^[a-záéíóúñA-ZÁÉÍÓÚÑ ]{5,100}$",
            null,
            "Nombre y apellidos completos",
            1
        ));

        // Email
        campos.add(new CampoFormulario(
            "email",
            "Correo Electrónico",
            CampoFormulario.TipoCampo.EMAIL,
            true,
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            null,
            "Correo válido para notificaciones",
            2
        ));

        // Teléfono
        campos.add(new CampoFormulario(
            "telefono",
            "Teléfono de Contacto",
            CampoFormulario.TipoCampo.PHONE,
            true,
            "^[0-9]{7,15}$",
            null,
            "Número de teléfono sin espacios",
            3
        ));

        // Dirección
        campos.add(new CampoFormulario(
            "direccion",
            "Dirección Completa",
            CampoFormulario.TipoCampo.TEXTAREA,
            true,
            null,
            null,
            "Calle, número, apartamento, ciudad",
            4
        ));

        // Fecha de Solicitud
        campos.add(new CampoFormulario(
            "fechaSolicitud",
            "Fecha de la Solicitud",
            CampoFormulario.TipoCampo.DATE,
            true,
            null,
            null,
            "Fecha en que realiza la solicitud",
            5
        ));

        // Tipo de Servicio
        List<CampoFormulario.OpcionCampo> tiposServicio = new ArrayList<>();
        tiposServicio.add(new CampoFormulario.OpcionCampo("residencial", "Residencial"));
        tiposServicio.add(new CampoFormulario.OpcionCampo("comercial", "Comercial"));
        tiposServicio.add(new CampoFormulario.OpcionCampo("industrial", "Industrial"));
        
        campos.add(new CampoFormulario(
            "tipoServicio",
            "Tipo de Servicio",
            CampoFormulario.TipoCampo.SELECT,
            true,
            null,
            tiposServicio,
            "Seleccione el tipo de servicio que necesita",
            6
        ));

        // Descripción de la Solicitud
        campos.add(new CampoFormulario(
            "descripcionSolicitud",
            "Descripción de la Solicitud",
            CampoFormulario.TipoCampo.TEXTAREA,
            true,
            null,
            null,
            "Detalle específico de lo que solicita",
            7
        ));

        // Acepta términos
        campos.add(new CampoFormulario(
            "aceptaTerminos",
            "Acepto los términos y condiciones",
            CampoFormulario.TipoCampo.CHECKBOX,
            true,
            null,
            null,
            "Debe aceptar los términos para continuar",
            8
        ));

        return campos;
    }
}