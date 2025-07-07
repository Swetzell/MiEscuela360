package com.miescuela360.config;

import com.miescuela360.model.Permiso;
import com.miescuela360.model.Rol;
import com.miescuela360.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class RolConfig {

    @Bean
    CommandLineRunner iniciarRoles(RolRepository rolRepository) {
        return args -> {
            // Verificar si ya existen roles
            if (rolRepository.count() == 0) {
                // Crear rol de administrador con todos los permisos
                Rol rolAdmin = new Rol("ROLE_ADMIN");
                rolAdmin.setDescripcion("Acceso completo al sistema");
                rolAdmin.setPermisos(new HashSet<>(Arrays.asList(
                    Permiso.ACCESO_ASISTENCIAS,
                    Permiso.ACCESO_ALUMNOS,
                    Permiso.ACCESO_GRADOS,
                    Permiso.ACCESO_SECCIONES,
                    Permiso.ACCESO_USUARIOS,
                    Permiso.ACCESO_ROLES,
                    Permiso.ACCESO_SERVICIOS,
                    Permiso.ACCESO_CONFIGURACION
                )));
                
                // Crear rol de maestro con permisos limitados
                Rol rolMaestra = new Rol("ROLE_MAESTRA");
                rolMaestra.setDescripcion("Acceso a gestión de alumnos y asistencias");
                rolMaestra.setPermisos(new HashSet<>(Arrays.asList(
                    Permiso.ACCESO_ASISTENCIAS,
                    Permiso.ACCESO_ALUMNOS,
                    Permiso.ACCESO_GRADOS,
                    Permiso.ACCESO_SECCIONES
                )));
                
                // Guardar roles en la base de datos
                rolRepository.save(rolAdmin);
                rolRepository.save(rolMaestra);
            }
        };
    }
}