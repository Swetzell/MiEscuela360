package com.miescuela360.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.miescuela360.model.Usuario;
import com.miescuela360.service.UsuarioService;
import com.miescuela360.service.RolService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.findAll());
        return "usuarios/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolService.findAll());
        return "usuarios/form";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.save(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado exitosamente");
            return "redirect:/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el usuario: " + e.getMessage());
            return "redirect:/usuarios/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            model.addAttribute("usuario", usuario);
            model.addAttribute("roles", rolService.findAll());
            return "usuarios/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el usuario: " + e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/activar/{id}")
    public String activarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.activarDesactivar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Estado del usuario actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el estado del usuario: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }
} 