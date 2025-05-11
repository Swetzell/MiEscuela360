package com.sudamericana.impoexcel.config;

import com.sudamericana.impoexcel.service.AutorizacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AccesoInterceptor implements HandlerInterceptor {

    @Autowired
    private AutorizacionService autorizacionService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // No verificar acceso para recursos estáticos o páginas públicas
        if (requestURI.startsWith("/css/") || 
            requestURI.startsWith("/js/") || 
            requestURI.startsWith("/img/") ||
            requestURI.equals("/login") ||
            requestURI.equals("/error") ||
            requestURI.equals("/acceso-denegado") ||
            // Solo permitir automáticamente rutas administrativas críticas básicas
            requestURI.equals("/")) {
            return true;
        }
    
        // Verificación especial para usuarios
        if (requestURI.startsWith("/usuarios")) {
            if (!autorizacionService.tieneAccesoAGestionUsuarios()) {
                response.sendRedirect("/acceso-denegado");
                return false;
            }
            return true;
        }
        
        // Verificación especial para servicios
        if (requestURI.startsWith("/servicios")) {
            if (!autorizacionService.tieneAccesoAGestionServicios()) {
                response.sendRedirect("/acceso-denegado");
                return false;
            }
            return true;
        }
        
        // Verificar si el usuario tiene acceso al servicio solicitado
        if (!autorizacionService.tieneAccesoAServicio(requestURI)) {
            response.sendRedirect("/acceso-denegado");
            return false;
        }
        
        return true;
    }
}
