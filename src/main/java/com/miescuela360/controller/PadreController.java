package com.miescuela360.controller;

import com.miescuela360.model.Padre;
import com.miescuela360.service.PadreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/padres")
public class PadreController {

    @Autowired
    private PadreService padreService;

    @GetMapping
    public String listarPadres(Model model) {
        model.addAttribute("padres", padreService.findAll());
        return "padres/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("padre", new Padre());
        return "padres/form";
    }

    @PostMapping("/guardar")
    public String guardarPadre(@ModelAttribute Padre padre, RedirectAttributes redirectAttributes) {
        try {
            if (padreService.existsByDni(padre.getDni())) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un padre con ese DNI");
                return "redirect:/padres/nuevo";
            }
            padreService.save(padre);
            redirectAttributes.addFlashAttribute("mensaje", "Padre guardado exitosamente");
            return "redirect:/padres";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el padre: " + e.getMessage());
            return "redirect:/padres/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Padre padre = padreService.findById(id)
            .orElseThrow(() -> new RuntimeException("Padre no encontrado"));
        model.addAttribute("padre", padre);
        return "padres/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPadre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            padreService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Padre eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el padre: " + e.getMessage());
        }
        return "redirect:/padres";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Padre padre = padreService.findById(id)
            .orElseThrow(() -> new RuntimeException("Padre no encontrado"));
        model.addAttribute("padre", padre);
        return "padres/detalle";
    }
} 