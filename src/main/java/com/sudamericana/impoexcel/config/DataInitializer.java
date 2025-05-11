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
        createRolIfNotFound("ADMINISTRADOR");
        createRolIfNotFound("MAESTRA");

        //  usuario administrador 
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Set<Rol> adminRoles = new HashSet<>();
            rolRepository.findByNombre("ADMINISTRADOR").ifPresent(adminRoles::add);

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
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
