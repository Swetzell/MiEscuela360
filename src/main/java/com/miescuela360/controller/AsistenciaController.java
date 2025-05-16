package com.miescuela360.controller;

import com.miescuela360.model.Alumno;
import com.miescuela360.model.Asistencia;
import com.miescuela360.model.Usuario;
import com.miescuela360.service.AlumnoService;
import com.miescuela360.service.AsistenciaService;
import com.miescuela360.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listarAsistencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Model model) {
        
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        
        List<Asistencia> asistencias = asistenciaService.findByFechaOrderByAlumnoNombre(fecha);
        model.addAttribute("asistencias", asistencias);
        model.addAttribute("fecha", fecha);
        
        return "asistencias/index";
    }

    @GetMapping("/registrar")
    public String mostrarFormularioRegistro(Model model) {
        List<Alumno> alumnos = alumnoService.findAll().stream()
                .filter(Alumno::isActivo)
                .toList();
        
        LocalDate fechaHoy = LocalDate.now();
        
        List<Asistencia> asistencias = new ArrayList<>();
        
        for (Alumno alumno : alumnos) {
            List<Asistencia> asistenciasExistentes = asistenciaService.findByAlumnoAndFecha(alumno, fechaHoy);
            
            if (asistenciasExistentes.isEmpty()) {
                Asistencia asistencia = new Asistencia();
                asistencia.setAlumno(alumno);
                asistencia.setFecha(fechaHoy);
                asistencia.setHoraEntrada(LocalTime.now());
                asistencia.setEstado(Asistencia.EstadoAsistencia.PRESENTE);
                asistencias.add(asistencia);
            } else {
                asistencias.add(asistenciasExistentes.get(0));
            }
        }
        
        model.addAttribute("asistencias", asistencias);
        model.addAttribute("fecha", fechaHoy);
        model.addAttribute("estadosAsistencia", Asistencia.EstadoAsistencia.values());
        
        return "asistencias/registrar";
    }

    @PostMapping("/guardar")
    public String guardarAsistencias(
            @RequestParam("alumnoId") List<Long> alumnoIds,
            @RequestParam(name = "estado", required = false) List<String> estados,
            @RequestParam(name = "observaciones", required = false) List<String> observaciones,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElse(null);
        
        try {
            if (estados == null) estados = new ArrayList<>();
            if (observaciones == null) observaciones = new ArrayList<>();
            
            for (int i = 0; i < alumnoIds.size(); i++) {
                Long alumnoId = alumnoIds.get(i);
                
                String estadoStr = (i < estados.size()) ? estados.get(i) : "PRESENTE";
                String observacion = (i < observaciones.size()) ? observaciones.get(i) : "";
                
                List<Asistencia> asistenciasExistentes = asistenciaService.findByAlumnoIdAndFecha(alumnoId, fecha);
                Asistencia asistencia;
                
                if (asistenciasExistentes.isEmpty()) {
                    asistencia = new Asistencia();
                    Optional<Alumno> alumnoOpt = alumnoService.findById(alumnoId);
                    if (alumnoOpt.isPresent()) {
                        asistencia.setAlumno(alumnoOpt.get());
                    } else {
                        continue; 
                    }
                    asistencia.setFecha(fecha);
                    asistencia.setHoraEntrada(LocalTime.now());
                } else {
                    asistencia = asistenciasExistentes.get(0);
                }
                
                if (!estadoStr.isEmpty()) {
                    try {
                        asistencia.setEstado(Asistencia.EstadoAsistencia.valueOf(estadoStr));
                    } catch (IllegalArgumentException e) {
                        asistencia.setEstado(Asistencia.EstadoAsistencia.PRESENTE);
                    }
                } else {
                    asistencia.setEstado(Asistencia.EstadoAsistencia.PRESENTE);
                }
                
                asistencia.setObservaciones(observacion);
                asistencia.setRegistradoPor(usuario);
                
                asistenciaService.save(asistencia);
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "Asistencias guardadas exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar asistencias: " + e.getMessage());
            e.printStackTrace(); 
        }
        
        return "redirect:/asistencias";
    }
    @GetMapping("/editar/{id}")
    public String editarAsistencia(@PathVariable Long id, Model model) {
        Optional<Asistencia> asistenciaOpt = asistenciaService.findById(id);
        
        if (asistenciaOpt.isPresent()) {
            model.addAttribute("asistencia", asistenciaOpt.get());
            model.addAttribute("estadosAsistencia", Asistencia.EstadoAsistencia.values());
            return "asistencias/editar";
        } else {
            return "redirect:/asistencias";
        }
    }
    
    @PostMapping("/actualizar")
    public String actualizarAsistencia(@ModelAttribute Asistencia asistencia, RedirectAttributes redirectAttributes) {
        try {
            Optional<Asistencia> asistenciaExistente = asistenciaService.findById(asistencia.getId());
            
            if (asistenciaExistente.isPresent()) {
                Asistencia asistenciaActual = asistenciaExistente.get();
                
                asistenciaActual.setEstado(asistencia.getEstado());
                asistenciaActual.setObservaciones(asistencia.getObservaciones());
                
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Usuario usuario = usuarioService.findByUsername(auth.getName()).orElse(null);
                asistenciaActual.setRegistradoPor(usuario);
                
                asistenciaService.save(asistenciaActual);
                redirectAttributes.addFlashAttribute("mensaje", "Asistencia actualizada exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se encontró la asistencia a actualizar");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar asistencia: " + e.getMessage());
        }
        
        return "redirect:/asistencias";
    }
    
    @GetMapping("/eliminar/{id}")
    public String eliminarAsistencia(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            asistenciaService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Asistencia eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar asistencia: " + e.getMessage());
        }
        
        return "redirect:/asistencias";
    }
}