package com.miescuela360.controller;

import com.miescuela360.service.GradoService;
import com.miescuela360.service.SeccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/sistema")
public class SistemaController {

    @Autowired
    private GradoService gradoService;
    
    @Autowired
    private SeccionService seccionService;
    
    @PostMapping("/grados/inicializar")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> inicializarGrados() {
        try {
            gradoService.inicializarGrados();
            return ResponseEntity.ok("Grados inicializados correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al inicializar grados: " + e.getMessage());
        }
    }
    
    @PostMapping("/secciones/inicializar")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> inicializarSecciones() {
        try {
            seccionService.inicializarSecciones();
            return ResponseEntity.ok("Secciones inicializadas correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al inicializar secciones: " + e.getMessage());
        }
    }
}
