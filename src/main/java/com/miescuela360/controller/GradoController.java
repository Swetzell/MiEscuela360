package com.miescuela360.controller;

import com.miescuela360.model.Grado;
import com.miescuela360.service.GradoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/grados")
public class GradoController {
    
    @Autowired
    private GradoService gradoService;
    
    @GetMapping
    public String listarGrados(Model model) {
        model.addAttribute("grados", gradoService.findAll());
        return "grados/index";
    }
    
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Grado grado = new Grado();
        model.addAttribute("grado", grado);
        model.addAttribute("titulo", "Nuevo Grado");
        return "grados/form";
    }
    
    @PostMapping("/guardar")
    public String guardarGrado(@ModelAttribute Grado grado, RedirectAttributes redirectAttributes) {
        try {
            // Verificar si ya existe un grado con ese nombre
            if (grado.getId() == null && gradoService.existsByNombre(grado.getNombre())) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un grado con el nombre: " + grado.getNombre());
                return "redirect:/grados/nuevo";
            }
            
            gradoService.save(grado);
            redirectAttributes.addFlashAttribute("mensaje", "Grado guardado exitosamente");
            return "redirect:/grados";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el grado: " + e.getMessage());
            return "redirect:/grados/nuevo";
        }
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Grado grado = gradoService.findById(id)
            .orElseThrow(() -> new RuntimeException("Grado no encontrado"));
        
        model.addAttribute("grado", grado);
        model.addAttribute("titulo", "Editar Grado");
        return "grados/form";
    }
      @GetMapping("/eliminar/{id}")
    public String eliminarGrado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // En lugar de eliminar, marcamos como inactivo
            gradoService.disable(id);
            redirectAttributes.addFlashAttribute("mensaje", "Grado desactivado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al desactivar el grado: " + e.getMessage());
        }
        return "redirect:/grados";
    }

    @GetMapping("/toggle-estado/{id}")
    public String toggleEstadoGrado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            gradoService.toggleActive(id);
            String mensaje = gradoService.findById(id)
                .map(grado -> grado.getActivo() ? "Grado activado exitosamente" : "Grado desactivado exitosamente")
                .orElse("Estado del grado actualizado exitosamente");
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar el estado del grado: " + e.getMessage());
        }
        return "redirect:/grados";
    }
    
    @GetMapping("/inicializar")
    public String inicializarDatos(RedirectAttributes redirectAttributes) {
        try {
            gradoService.initDefaultData();
            redirectAttributes.addFlashAttribute("mensaje", "Datos de grados inicializados correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al inicializar datos: " + e.getMessage());
        }
        return "redirect:/grados";
    }
}
