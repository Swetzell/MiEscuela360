package com.sudamericana.impoexcel.service;

import com.sudamericana.impoexcel.model.Usuario;
import com.sudamericana.impoexcel.model.Servicio;
import com.sudamericana.impoexcel.model.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AutorizacionService {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ServicioService servicioService;
    
    /**
     * Verifica si el usuario tiene acceso a un servicio específico
     */
    public boolean tieneAccesoAServicio(String ruta) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Si no está autenticado, no tiene acceso
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Verificamos si es un administrador (tiene acceso a todo)
        if (esUsuarioAdmin(authentication)) {
            return true;
        }
        
        // Obtenemos el usuario y verificamos sus permisos específicos
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);
        
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Verificamos si el usuario tiene acceso al servicio solicitado
        return usuario.getServicios().stream()
                .anyMatch(servicio -> ruta.startsWith(servicio.getRuta()));
    }
    
    /**
     * Verifica específicamente si el usuario tiene acceso a la gestión de usuarios
     * (tiene rol ADMIN o tiene asignado el servicio de gestión de usuarios)
     */
    public boolean tieneAccesoAGestionUsuarios() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Si no está autenticado, no tiene acceso
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Verificamos si es admin por roles de Spring Security
        if (esUsuarioAdmin(authentication)) {
            return true;
        }
        
        // Obtenemos el usuario y verificamos sus permisos específicos
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);
        
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Verificar si tiene rol ADMIN en la base de datos
        return usuario.getRoles().stream().anyMatch(r -> 
                r.getNombre().equals("ADMINISTRADOR") || r.getNombre().equals("ROLE_ADMINISTRADOR"));
    }
    
    /**
     * Verifica si el usuario tiene rol ADMIN según Spring Security
     */
    private boolean esUsuarioAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    public boolean tieneAccesoAGestionServicios() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Si no está autenticado, no tiene acceso
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Verificamos si es admin por roles de Spring Security
        if (esUsuarioAdmin(authentication)) {
            return true;
        }
        
        // Obtenemos el usuario y verificamos sus permisos específicos
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorUsername(username);
        
        if (usuarioOpt.isEmpty()) {
            return false;
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Verificar si tiene rol ADMIN en la base de datos
        return usuario.getRoles().stream().anyMatch(r -> 
                r.getNombre().equals("ADMINISTRADOR") || r.getNombre().equals("ROLE_ADMINISTRADOR"));
    }
}