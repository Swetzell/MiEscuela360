package com.miescuela360.controller;

import com.miescuela360.model.Pago;
import com.miescuela360.model.Alumno;
import com.miescuela360.service.PagoService;
import com.miescuela360.service.AlumnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pagos")
public class PagoController {
    @Autowired
    private PagoService pagoService;
    @Autowired
    private AlumnoService alumnoService;

    @GetMapping
    public String listarPagos(Model model) {
        model.addAttribute("pagos", pagoService.findAll());
        return "pagos/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("pago", new Pago());
        model.addAttribute("alumnos", alumnoService.findAll());
        return "pagos/form";
    }

    @PostMapping("/guardar")
    public String guardarPago(@ModelAttribute Pago pago, RedirectAttributes redirectAttributes) {
        pagoService.save(pago);
        redirectAttributes.addFlashAttribute("mensaje", "Pago guardado exitosamente");
        return "redirect:/pagos";
    }

    @GetMapping("/editar/{id}")
    public String editarPago(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pago> pagoOpt = pagoService.findById(id);
        if (pagoOpt.isPresent()) {
            model.addAttribute("pago", pagoOpt.get());
            model.addAttribute("alumnos", alumnoService.findAll());
            return "pagos/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Pago no encontrado");
            return "redirect:/pagos";
        }
    }

    @GetMapping("/detalle/{id}")
    public String detallePago(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pago> pagoOpt = pagoService.findById(id);
        if (pagoOpt.isPresent()) {
            model.addAttribute("pago", pagoOpt.get());
            return "pagos/detalle";
        } else {
            redirectAttributes.addFlashAttribute("error", "Pago no encontrado");
            return "redirect:/pagos";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pagoService.deleteById(id);
        redirectAttributes.addFlashAttribute("mensaje", "Pago eliminado exitosamente");
        return "redirect:/pagos";
    }
}