package com.sudamericana.impoexcel.controller;

import com.sudamericana.impoexcel.model.Servicio;
import com.sudamericana.impoexcel.service.AutorizacionService;
import com.sudamericana.impoexcel.service.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/servicios")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private AutorizacionService autorizacionService;

    @Autowired
private JdbcTemplate jdbcTemplate;
    
@GetMapping
public String listarServicios(Model model) {
    // Verificar permisos
    if (!autorizacionService.tieneAccesoAGestionServicios()) {
        return "redirect:/acceso-denegado";
    }
    
    List<Servicio> servicios = servicioService.listarTodos();
    model.addAttribute("servicios", servicios);
    return "servicios/gestion";
}

    
    @PostMapping("/guardar")
    public String guardarServicio(@ModelAttribute Servicio servicio, RedirectAttributes redirectAttributes) {
        // Verificar permisos
        if (!autorizacionService.tieneAccesoAGestionUsuarios()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para realizar esta acción");
            return "redirect:/acceso-denegado";
        }
        
        // Determinar si es una creación o una actualización
        boolean esNuevo = (servicio.getId() == null || servicio.getId() == 0);
        
        System.out.println("Procesando servicio - ID: " + servicio.getId() + ", Nombre: " + servicio.getNombre() + ", Es nuevo: " + esNuevo);
        
        if (!servicio.getRuta().startsWith("/")) {
            servicio.setRuta("/" + servicio.getRuta());
        }
        
        try {
            if (esNuevo) {
                // Verificar que no existan servicios con el mismo nombre o ruta
                if (servicioService.buscarPorNombre(servicio.getNombre()).isPresent()) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Ya existe un servicio con el nombre '" + servicio.getNombre() + "'");
                    return "redirect:/servicios";
                }
                
                if (servicioService.buscarPorRuta(servicio.getRuta()).isPresent()) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Ya existe un servicio con la ruta '" + servicio.getRuta() + "'");
                    return "redirect:/servicios";
                }
                
                // Crear nuevo servicio
                servicioService.guardar(servicio);
                redirectAttributes.addFlashAttribute("successMessage", "Servicio creado con éxito");
            } else {
                // Actualización - buscar el servicio existente
                Optional<Servicio> existente = servicioService.buscarPorId(servicio.getId());
                if (existente.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "No se pudo encontrar el servicio con ID " + servicio.getId());
                    return "redirect:/servicios";
                }
                
                Servicio servicioExistente = existente.get();
                
                // Verificar que no exista otro servicio con el mismo nombre (si cambió)
                if (!servicioExistente.getNombre().equals(servicio.getNombre())) {
                    Optional<Servicio> servicioConMismoNombre = servicioService.buscarPorNombre(servicio.getNombre());
                    if (servicioConMismoNombre.isPresent() && !servicioConMismoNombre.get().getId().equals(servicio.getId())) {
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Ya existe otro servicio con el nombre '" + servicio.getNombre() + "'");
                        return "redirect:/servicios";
                    }
                }
                
                // Verificar que no exista otro servicio con la misma ruta (si cambió)
                if (!servicioExistente.getRuta().equals(servicio.getRuta())) {
                    Optional<Servicio> servicioConMismaRuta = servicioService.buscarPorRuta(servicio.getRuta());
                    if (servicioConMismaRuta.isPresent() && !servicioConMismaRuta.get().getId().equals(servicio.getId())) {
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Ya existe otro servicio con la ruta '" + servicio.getRuta() + "'");
                        return "redirect:/servicios";
                    }
                }
                
                // Actualizar campos del servicio existente
                servicioExistente.setNombre(servicio.getNombre());
                servicioExistente.setDescripcion(servicio.getDescripcion());
                servicioExistente.setRuta(servicio.getRuta());
                
                // Guardar cambios
                servicioService.guardar(servicioExistente);
                redirectAttributes.addFlashAttribute("successMessage", "Servicio actualizado con éxito");
            }
        } catch (Exception e) {
            System.err.println("Error al procesar servicio: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al " + (esNuevo ? "crear" : "actualizar") + " el servicio: " + e.getMessage());
        }
        
        return "redirect:/servicios";
    }

    @GetMapping("/editar/{id}")
    @ResponseBody
    public Servicio obtenerServicio(@PathVariable Long id) {
        // Verificar permisos
        if (!autorizacionService.tieneAccesoAGestionUsuarios()) {
            return null;
        }
        
        Optional<Servicio> servicioOpt = servicioService.buscarPorId(id);
        if (servicioOpt.isEmpty()) {
            System.err.println("No se encontró servicio con ID: " + id);
            return new Servicio(); // Devolver objeto vacío en caso de error
        }
        
        Servicio servicio = servicioOpt.get();
        System.out.println("Servicio encontrado para editar - ID: " + servicio.getId() + 
                           ", Nombre: " + servicio.getNombre() + 
                           ", Descripción: " + servicio.getDescripcion() + 
                           ", Ruta: " + servicio.getRuta());
        
        return servicio;
    }
    @PostMapping("/eliminar/{id}")
    public String eliminarServicio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Verificar permisos
        if (!autorizacionService.tieneAccesoAGestionUsuarios()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para realizar esta acción");
            return "redirect:/acceso-denegado";
        }
        
        try {
            // Primero eliminar las relaciones con usuarios
            // Podemos usar un enfoque directo con JDBC
            jdbcTemplate.update("DELETE FROM usuario_servicios WHERE servicio_id = ?", id);
            
            // Luego eliminar el servicio
            servicioService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", "Servicio eliminado con éxito");
        } catch (Exception e) {
            System.err.println("Error al eliminar servicio: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el servicio: " + e.getMessage());
        }
        
        return "redirect:/servicios";
    }
}