package com.miescuela360.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miescuela360.model.Auditoria;
import com.miescuela360.model.Usuario;
import com.miescuela360.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final UsuarioService usuarioService;
    
    @Autowired
    private AuditoriaRepository auditoriaRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public AuditoriaService(@Lazy UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    
    @Transactional
    public void registrarAccion(String accion, Object entidad, Object datosAnteriores) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Auth: " + auth);
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String username = auth.getName();
                System.out.println("Usuario autenticado: " + username);
                Usuario usuario = usuarioService.findByUsername(username).orElse(null);
                System.out.println("Usuario encontrado: " + usuario);
                if (usuario != null) {
                    String ip = obtenerIPCliente();
                    
                    Auditoria auditoria = new Auditoria();
                    auditoria.setAccion(accion);
                    auditoria.setEntidad(entidad.getClass().getSimpleName());
                    auditoria.setEntidadId(obtenerId(entidad));
                    auditoria.setUsuario(usuario);
                    auditoria.setFechaHora(LocalDateTime.now());
                    auditoria.setIp(ip);
                    
                    if (datosAnteriores != null) {
                        auditoria.setDatosAnteriores(objectMapper.writeValueAsString(datosAnteriores));
                    }
                    
                    if (entidad != null) {
                        auditoria.setDatosNuevos(objectMapper.writeValueAsString(entidad));
                    }
                    
                    auditoriaRepository.save(auditoria);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Long obtenerId(Object entidad) {
        try {
            return (Long) entidad.getClass().getMethod("getId").invoke(entidad);
        } catch (Exception e) {
            return null;
        }
    }
    
    private String obtenerIPCliente() {
        try {
            HttpServletRequest request = 
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ip = request.getHeader("X-FORWARDED-FOR");
            return (ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip;
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }
}