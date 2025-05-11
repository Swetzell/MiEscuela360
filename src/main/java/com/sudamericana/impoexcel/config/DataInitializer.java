package com.sudamericana.impoexcel.config;

import com.sudamericana.impoexcel.model.Rol;
import com.sudamericana.impoexcel.model.Usuario;
import com.sudamericana.impoexcel.repository.RolRepository;
import com.sudamericana.impoexcel.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Crear roles si no existen
        createRolIfNotFound("ADMIN");
        createRolIfNotFound("IMPORTACION");
        createRolIfNotFound("VENTAS");
        createRolIfNotFound("FINANZAS");
        createRolIfNotFound("SISTEMAS");
        createRolIfNotFound("LOGISTICA");
        createRolIfNotFound("GERENCIA");

        // Crear usuarios de ejemplo si no existen
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Set<Rol> adminRoles = new HashSet<>();
            rolRepository.findByNombre("ADMIN").ifPresent(adminRoles::add);

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setActivo(true);
            admin.setRoles(adminRoles);
            usuarioRepository.save(admin);
        }

        // Usuario de importación
        if (usuarioRepository.findByUsername("importacion").isEmpty()) {
            Set<Rol> roles = new HashSet<>();
            rolRepository.findByNombre("IMPORTACION").ifPresent(roles::add);

            Usuario user = new Usuario();
            user.setUsername("importacion");
            user.setPassword(passwordEncoder.encode("import123"));
            user.setNombre("Usuario");
            user.setApellido("Importación");
            user.setActivo(true);
            user.setRoles(roles);
            usuarioRepository.save(user);
        }

        // Usuario de Sra.Maribel roles administrativos
        if (usuarioRepository.findByUsername("maribel").isEmpty()) {
            Set<Rol> adminRoles = new HashSet<>();
            rolRepository.findByNombre("ADMIN").ifPresent(adminRoles::add);

            Usuario admin = new Usuario();
            admin.setUsername("maribel");
            admin.setPassword(passwordEncoder.encode("Maribel2025"));
            admin.setNombre("Maribel");
            admin.setApellido("Camilo");
            admin.setActivo(true);
            admin.setRoles(adminRoles);
            usuarioRepository.save(admin);
        }
    }

    private void createRolIfNotFound(String nombre) {
        rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            return rolRepository.save(rol);
        });
    }
}
