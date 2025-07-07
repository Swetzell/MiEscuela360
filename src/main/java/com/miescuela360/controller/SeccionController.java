package com.miescuela360.controller;

import com.miescuela360.model.Seccion;
import com.miescuela360.service.SeccionService;
import com.miescuela360.service.GradoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/secciones")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MAESTRA')")
public class SeccionController {
    
    @Autowired
    private SeccionService seccionService;
    
    @Autowired
    private GradoService gradoService;
    
    @GetMapping
    public String listarSecciones(Model model) {
        model.addAttribute("secciones", seccionService.findAll());
        return "secciones/index";
    }
    
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Seccion seccion = new Seccion();
        model.addAttribute("seccion", seccion);
        model.addAttribute("grados", gradoService.findAll());
        model.addAttribute("titulo", "Nueva Sección");
        return "secciones/form";
    }
    
    @PostMapping("/guardar")
    public String guardarSeccion(@ModelAttribute Seccion seccion, RedirectAttributes redirectAttributes) {
        try {
            // Verificar si ya existe una sección con ese nombre
            if (seccion.getId() == null && seccionService.existsByNombre(seccion.getNombre())) {
                redirectAttributes.addFlashAttribute("error", "Ya existe una sección con el nombre: " + seccion.getNombre());
                return "redirect:/secciones/nuevo";
            }
            
            seccionService.save(seccion);
            redirectAttributes.addFlashAttribute("mensaje", "Sección guardada exitosamente");
            return "redirect:/secciones";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la sección: " + e.getMessage());
            return "redirect:/secciones/nuevo";
        }
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Seccion seccion = seccionService.findById(id)
            .orElseThrow(() -> new RuntimeException("Sección no encontrada"));
        
        model.addAttribute("seccion", seccion);
        model.addAttribute("grados", gradoService.findAll());
        model.addAttribute("titulo", "Editar Sección");
        return "secciones/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarSeccion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // En lugar de eliminar, marcamos como inactivo
            seccionService.disable(id);
            redirectAttributes.addFlashAttribute("mensaje", "Sección desactivada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al desactivar la sección: " + e.getMessage());
        }
        return "redirect:/secciones";
    }

    @GetMapping("/toggle-estado/{id}")
    public String toggleEstadoSeccion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            seccionService.toggleActive(id);
            String mensaje = seccionService.findById(id)
                .map(seccion -> seccion.getActivo() ? "Sección activada exitosamente" : "Sección desactivada exitosamente")
                .orElse("Estado de la sección actualizado exitosamente");
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar el estado de la sección: " + e.getMessage());
        }
        return "redirect:/secciones";
    }
    
    @GetMapping("/inicializar")
    public String inicializarDatos(RedirectAttributes redirectAttributes) {
        try {
            seccionService.initDefaultData();
            redirectAttributes.addFlashAttribute("mensaje", "Datos de secciones inicializados correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al inicializar datos: " + e.getMessage());
        }
        return "redirect:/secciones";
    }
}
