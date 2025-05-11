package com.miescuela360.controller;

import com.miescuela360.model.Madre;
import com.miescuela360.service.MadreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/madres")
public class MadreController {

    @Autowired
    private MadreService madreService;

    @GetMapping
    public String listarMadres(Model model) {
        model.addAttribute("madres", madreService.findAll());
        return "madres/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("madre", new Madre());
        return "madres/form";
    }

    @PostMapping("/guardar")
    public String guardarMadre(@ModelAttribute Madre madre, RedirectAttributes redirectAttributes) {
        try {
            if (madreService.existsByDni(madre.getDni())) {
                redirectAttributes.addFlashAttribute("error", "Ya existe una madre con ese DNI");
                return "redirect:/madres/nuevo";
            }
            madreService.save(madre);
            redirectAttributes.addFlashAttribute("mensaje", "Madre guardada exitosamente");
            return "redirect:/madres";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la madre: " + e.getMessage());
            return "redirect:/madres/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Madre madre = madreService.findById(id)
            .orElseThrow(() -> new RuntimeException("Madre no encontrada"));
        model.addAttribute("madre", madre);
        return "madres/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarMadre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            madreService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Madre eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la madre: " + e.getMessage());
        }
        return "redirect:/madres";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Madre madre = madreService.findById(id)
            .orElseThrow(() -> new RuntimeException("Madre no encontrada"));
        model.addAttribute("madre", madre);
        return "madres/detalle";
    }
} 