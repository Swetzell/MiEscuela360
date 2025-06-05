package com.miescuela360.controller;

import com.miescuela360.model.Alumno;
import com.miescuela360.service.AlumnoService;
import com.miescuela360.service.PadreService;
import com.miescuela360.service.MadreService;
import com.miescuela360.service.GradoService;
import com.miescuela360.service.SeccionService;
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
        System.out.println("Validando formato de DNI: " + dni);
        
        // Validar formato del DNI (8 dígitos numéricos)
        if (dni == null || !dni.matches("^\\d{8}$")) {
            return ResponseEntity.ok(new DniResponse(false, dni, "El DNI debe contener exactamente 8 dígitos numéricos"));
        }
        
        // Si el formato es correcto, devolvemos éxito
        // No validamos contra API externa, solo el formato
        return ResponseEntity.ok(new DniResponse(true, dni, "DNI válido"));
    }
}