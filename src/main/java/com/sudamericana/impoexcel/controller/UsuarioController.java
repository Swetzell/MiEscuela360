package com.sudamericana.impoexcel.controller;

import com.sudamericana.impoexcel.model.Usuario;
import com.sudamericana.impoexcel.model.Servicio;
import com.sudamericana.impoexcel.service.UsuarioService;
import com.sudamericana.impoexcel.service.RolService;
import com.sudamericana.impoexcel.service.ServicioService;
import com.sudamericana.impoexcel.service.AutorizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private RolService rolService;
    
    @Autowired
    private ServicioService servicioService;
    
    @Autowired
    private AutorizacionService autorizacionService;
    
    @GetMapping
    public String listarUsuarios(Model model) {
        return "redirect:/usuarios/gestion";
    }
    
    @GetMapping("/gestion")
    public String gestionPersonal(Model model) {
        // Verificación de permisos
        if (!autorizacionService.tieneAccesoAGestionUsuarios()) {
            return "redirect:/acceso-denegado";
        }
        
        List<Usuario> usuarios = usuarioService.obtenerTodos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("rolesDisponibles", rolService.listarTodos());
        model.addAttribute("serviciosDisponibles", servicioService.listarTodos());
        return "gestionpersonal";
    }
    
    @PostMapping("/guardar")
    public String guardarUsuario(
            @ModelAttribute Usuario usuario,
            @RequestParam(value = "serviciosIds", required = false) Long[] serviciosIds,
            RedirectAttributes redirectAttributes) {
        
        // Verificación de permisos
        if (!autorizacionService.tieneAccesoAGestionUsuarios()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para realizar esta acción");
            return "redirect:/acceso-denegado";
        }
        
        boolean esNuevo = (usuario.getId() == null);
        
        if (esNuevo && usuarioService.existeUsername(usuario.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", "El nombre de usuario ya existe");
            return "redirect:/usuarios/gestion";
        }
        
        try {
            // Asignar los servicios seleccionados
            Set<Servicio> serviciosSeleccionados = new HashSet<>();
            if (serviciosIds != null) {
                for (Long servicioId : serviciosIds) {
                    servicioService.buscarPorId(servicioId).ifPresent(serviciosSeleccionados::add);
                }
            }
            usuario.setServicios(serviciosSeleccionados);
            
            if (esNuevo) {
                usuarioService.crearUsuario(usuario);
                redirectAttributes.addFlashAttribute("successMessage", "Usuario creado con éxito");
            } else {
                usuarioService.actualizarUsuario(usuario);
                redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado con éxito");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el usuario: " + e.getMessage());
            return "redirect:/usuarios/gestion";
        }
        
        return "redirect:/usuarios/gestion";
    }
    
    @GetMapping("/editar/{id}")
    @ResponseBody
    public Usuario obtenerUsuario(@PathVariable Long id) {
        // Verificación de permisos
        if (!autorizacionService.tieneAccesoAGestionUsuarios()) {
            return null;
        }
        return usuarioService.obtenerPorId(id).orElse(null);
    }
    
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Verificación de permisos
        if (!autorizacionService.tieneAccesoAGestionUsuarios()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para realizar esta acción");
            return "redirect:/acceso-denegado";
        }
        
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/usuarios/gestion";
    }
}