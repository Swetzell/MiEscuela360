package com.miescuela360.config;

import com.miescuela360.model.Rol;
import com.miescuela360.model.Servicio;
import com.miescuela360.model.Usuario;
import com.miescuela360.repository.RolRepository;
import com.miescuela360.repository.ServicioRepository;
import com.miescuela360.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear servicios si no existen
        if (servicioRepository.count() == 0) {
            // Servicios para Administrador
            Servicio gestionUsuarios = new Servicio();
            gestionUsuarios.setCodigo("GESTION_USUARIOS");
            gestionUsuarios.setNombre("Gestión de Usuarios");
            gestionUsuarios.setDescripcion("Permite gestionar usuarios del sistema");
            servicioRepository.save(gestionUsuarios);

            Servicio gestionRoles = new Servicio();
            gestionRoles.setCodigo("GESTION_ROLES");
            gestionRoles.setNombre("Gestión de Roles");
            gestionRoles.setDescripcion("Permite gestionar roles y permisos");
            servicioRepository.save(gestionRoles);

            // Servicios para Maestra
            Servicio gestionEstudiantes = new Servicio();
            gestionEstudiantes.setCodigo("GESTION_ESTUDIANTES");
            gestionEstudiantes.setNombre("Gestión de Estudiantes");
            gestionEstudiantes.setDescripcion("Permite gestionar estudiantes");
            servicioRepository.save(gestionEstudiantes);

            Servicio gestionPagos = new Servicio();
            gestionPagos.setCodigo("GESTION_PAGOS");
            gestionPagos.setNombre("Gestión de Pagos");
            gestionPagos.setDescripcion("Permite gestionar pagos de estudiantes");
            servicioRepository.save(gestionPagos);
        }

        // Crear roles si no existen
        if (rolRepository.count() == 0) {
            // Rol de Administrador
            Rol rolAdmin = new Rol();
            rolAdmin.setNombre("ROLE_ADMIN");
            rolAdmin.setDescripcion("Administrador del sistema con acceso total");
            Set<Servicio> serviciosAdmin = new HashSet<>();
            serviciosAdmin.add(servicioRepository.findByCodigo("GESTION_USUARIOS").orElseThrow());
            serviciosAdmin.add(servicioRepository.findByCodigo("GESTION_ROLES").orElseThrow());
            rolAdmin.setServicios(serviciosAdmin);
            rolRepository.save(rolAdmin);

            // Rol de Maestra
            Rol rolMaestra = new Rol();
            rolMaestra.setNombre("ROLE_MAESTRA");
            rolMaestra.setDescripcion("Maestra con acceso a gestión de estudiantes y pagos");
            Set<Servicio> serviciosMaestra = new HashSet<>();
            serviciosMaestra.add(servicioRepository.findByCodigo("GESTION_ESTUDIANTES").orElseThrow());
            serviciosMaestra.add(servicioRepository.findByCodigo("GESTION_PAGOS").orElseThrow());
            rolMaestra.setServicios(serviciosMaestra);
            rolRepository.save(rolMaestra);
        }

        // Crear usuario administrador si no existe
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setEmail("admin@miescuela360.com");
            admin.setActivo(true);

            Set<Rol> roles = new HashSet<>();
            roles.add(rolRepository.findByNombre("ROLE_ADMIN").orElseThrow());
            admin.setRoles(roles);

            usuarioRepository.save(admin);
        }
    }
} 