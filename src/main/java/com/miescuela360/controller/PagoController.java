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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pagos")
public class PagoController {
    @Autowired
    private PagoService pagoService;
    @Autowired
    private AlumnoService alumnoService;    @GetMapping
    public String listarPagos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            Model model) {
        List<Pago> pagos;
        
        Pago.EstadoPago estadoFiltro = null;
        if (estado != null && !estado.isEmpty()) {
            try {
                estadoFiltro = Pago.EstadoPago.valueOf(estado);
            } catch (IllegalArgumentException e) {
            }
        }
        
        if (fechaInicio != null && fechaFin != null) {
            try {
                if (estadoFiltro != null) {
                    pagos = pagoService.filtrarPagos(fechaInicio, fechaFin, estadoFiltro);
                } else {
                    pagos = pagoService.filtrarPagos(fechaInicio, fechaFin, null);
                }
            } catch (Exception e) {
                pagos = pagoService.findAll();
            }
        } else {
            if (estadoFiltro != null) {
                pagos = pagoService.obtenerPagosPorEstado(estadoFiltro);
            } else {
                pagos = pagoService.findAll();
            }
        }
        model.addAttribute("pagos", pagos);
        
        model.addAttribute("estados", pagoService.obtenerTodosLosEstados());
        model.addAttribute("estadoFiltro", estadoFiltro);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        
        try {
            model.addAttribute("totalPagos", pagoService.contarTotalPagos());
        } catch (Exception e) {
            model.addAttribute("totalPagos", 0);
        }
        
        try {
            model.addAttribute("pagosPendientes", pagoService.contarPagosPorEstado(Pago.EstadoPago.PENDIENTE));
        } catch (Exception e) {
            model.addAttribute("pagosPendientes", 0);
        }
          try {
            model.addAttribute("pagosPagados", pagoService.contarPagosPorEstado(Pago.EstadoPago.PAGADO));
        } catch (Exception e) {
            model.addAttribute("pagosPagados", 0);
        }
        
        try {
            Double montoTotal = pagoService.sumMontoByEstado(Pago.EstadoPago.PAGADO);
            model.addAttribute("montoTotal", montoTotal != null ? montoTotal : 0.0);
        } catch (Exception e) {
            model.addAttribute("montoTotal", 0.0);
        }
        
        return "pagos/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("pago", new Pago());
        model.addAttribute("alumnos", alumnoService.findAll());
        model.addAttribute("tiposPago", pagoService.obtenerTodosLosTiposPago());
        model.addAttribute("estados", pagoService.obtenerTodosLosEstados());
        return "pagos/form";
    }    @PostMapping("/guardar")
    public String guardarPago(
            @ModelAttribute Pago pago, 
            @RequestParam(required = false) boolean pagoInmediato,
            @RequestParam(required = false) LocalDate fechaPago,
            RedirectAttributes redirectAttributes) {
        
        if (pago.getId() == null && pagoInmediato) {
            pago.setEstado(Pago.EstadoPago.PAGADO);
            pago.setFechaPago(fechaPago != null ? fechaPago : LocalDate.now());
        }
        
        try {
            pagoService.save(pago);
            redirectAttributes.addFlashAttribute("mensaje", "Pago guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el pago: " + e.getMessage());
        }
        
        return "redirect:/pagos";
    }

    @GetMapping("/editar/{id}")
    public String editarPago(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pago> pagoOpt = pagoService.findById(id);
        if (pagoOpt.isPresent()) {
            model.addAttribute("pago", pagoOpt.get());
            model.addAttribute("alumnos", alumnoService.findAll());
            model.addAttribute("tiposPago", pagoService.obtenerTodosLosTiposPago());
            model.addAttribute("estados", pagoService.obtenerTodosLosEstados());
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

    @GetMapping("/procesar/{id}")
    public String procesarPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Pago pago = pagoService.procesarPago(id);
        if (pago != null) {
            redirectAttributes.addFlashAttribute("mensaje", "Pago procesado exitosamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo procesar el pago");
        }
        return "redirect:/pagos";
    }

    @GetMapping("/anular/{id}")
    public String anularPago(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Pago pago = pagoService.anularPago(id);
        if (pago != null) {
            redirectAttributes.addFlashAttribute("mensaje", "Pago anulado exitosamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo anular el pago");
        }
        return "redirect:/pagos";
    }
}