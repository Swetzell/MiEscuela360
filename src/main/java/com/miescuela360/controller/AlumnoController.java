package com.miescuela360.controller;

import com.miescuela360.model.Alumno;
import com.miescuela360.model.Padre;
import com.miescuela360.model.Madre;
import com.miescuela360.service.AlumnoService;
import com.miescuela360.service.PadreService;
import com.miescuela360.service.MadreService;
import com.miescuela360.service.GradoService;
import com.miescuela360.service.SeccionService;
import com.miescuela360.service.DniApiService;
import com.miescuela360.service.DniApiServiceAlternative;
import com.miescuela360.service.ApiFreeService;
import com.miescuela360.service.DniApiService.DniResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/alumnos")
public class AlumnoController {

    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private PadreService padreService;

    @Autowired
    private MadreService madreService;
    
    @Autowired
    private GradoService gradoService;
    
    @Autowired
    private SeccionService seccionService;
    
    @Autowired
    private DniApiService dniApiService;
    
    @Autowired
    private DniApiServiceAlternative dniApiServiceAlt;
    
    @Autowired
    private ApiFreeService apiFreeService;    // Ya no usamos estas listas estáticas, ahora usamos los servicios
    // private final List<String> grados = Arrays.asList("1°", "2°", "3°", "4°", "5°", "6°");
    // private final List<String> secciones = Arrays.asList("A", "B", "C", "D");

    @GetMapping
    public String listarAlumnos(Model model) {
        model.addAttribute("alumnos", alumnoService.findAll());
        return "alumnos/index";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Alumno alumno = new Alumno();
        alumno.setFechaInscripcion(LocalDate.now());
        model.addAttribute("alumno", alumno);
        model.addAttribute("padres", padreService.findAll());
        model.addAttribute("madres", madreService.findAll());
        model.addAttribute("grados", gradoService.findAllActive());
        model.addAttribute("secciones", seccionService.findAllActive());
        return "alumnos/form";
    }

    @PostMapping("/guardar")
    public String guardarAlumno(@ModelAttribute Alumno alumno, RedirectAttributes redirectAttributes) {
        try {
            alumnoService.save(alumno);
            redirectAttributes.addFlashAttribute("mensaje", "Alumno guardado exitosamente");
            return "redirect:/alumnos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el alumno: " + e.getMessage());
            return "redirect:/alumnos/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Alumno alumno = alumnoService.findById(id)
            .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
        model.addAttribute("alumno", alumno);
        model.addAttribute("padres", padreService.findAll());
        model.addAttribute("madres", madreService.findAll());
        model.addAttribute("grados", gradoService.findAllActive());
        model.addAttribute("secciones", seccionService.findAllActive());
        return "alumnos/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarAlumno(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            alumnoService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Alumno eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el alumno: " + e.getMessage());
        }
        return "redirect:/alumnos";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Alumno alumno = alumnoService.findById(id)
            .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
        model.addAttribute("alumno", alumno);
        return "alumnos/detalle";
    }    @PostMapping("/validar-dni")
    @ResponseBody
    public ResponseEntity<DniResponse> validarDni(@RequestParam("dni") String dni) {
        System.out.println("Recibida solicitud para validar DNI: " + dni);
        
        try {
            // ESTRATEGIA 1: API gratuita (más confiable)
            System.out.println("Intentando con API gratuita primero...");
            DniResponse freeResponse = apiFreeService.consultarDni(dni);
            
            if (freeResponse != null && freeResponse.isSuccess()) {
                System.out.println("API gratuita devolvió resultado exitoso.");
                return ResponseEntity.ok(freeResponse);
            }
            
            // ESTRATEGIA 2: API MiGo (original)
            System.out.println("Intentando con API MiGo...");
            DniResponse migoResponse = dniApiService.consultarDni(dni);
            
            if (migoResponse != null && migoResponse.isSuccess()) {
                System.out.println("API MiGo devolvió resultado exitoso.");
                return ResponseEntity.ok(migoResponse);
            } else if (migoResponse != null) {
                // Si hay un mensaje de error específico, lo preservamos para diagnóstico
                System.out.println("API MiGo falló: " + migoResponse.getNombre());
            }
            
            // ESTRATEGIA 3: API alternativa antigua
            System.out.println("Intentando con API alternativa antigua...");
            DniApiServiceAlternative.DniResponse altResponse = dniApiServiceAlt.consultarDni(dni);
            
            if (altResponse != null && altResponse.isSuccess()) {
                System.out.println("API alternativa antigua devolvió resultado exitoso.");
                return ResponseEntity.ok(new DniResponse(
                    altResponse.isSuccess(), 
                    altResponse.getDni(), 
                    altResponse.getNombre()
                ));
            }
            
            // Si llegamos aquí, ninguna API tuvo éxito
            System.out.println("Ninguna API pudo validar el DNI.");
            return ResponseEntity.ok(new DniResponse(false, dni, 
                "No se pudo validar el DNI en ninguna de las APIs disponibles. Por favor, verifique el número e intente nuevamente."));
        } catch (Exception e) {
            System.out.println("Error al validar DNI: " + e.getMessage());
            e.printStackTrace();
            DniResponse errorResponse = new DniResponse(false, dni, "Error: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
}