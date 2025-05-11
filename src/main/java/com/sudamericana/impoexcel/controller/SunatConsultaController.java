package com.sudamericana.impoexcel.controller;

import com.sudamericana.impoexcel.model.ClienteSunat;
import com.sudamericana.impoexcel.model.ConsultaSunat;
import com.sudamericana.impoexcel.service.RetenedorConsultaService;
import com.sudamericana.impoexcel.service.SunatConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/consulta-sunat")
public class SunatConsultaController {
    
    private static final Logger logger = LoggerFactory.getLogger(SunatConsultaController.class);
    
    @Autowired
    private SunatConsultaService sunatService;

    
    // Página principal de consulta individual
    @GetMapping
    public String mostrarPaginaConsulta(Model model) {
        return "consulta-sunat/consulta-individual";
    }
    
    // Página de resultados
    @GetMapping("/resultados")
    public String mostrarResultados(Model model) {
        try {
            List<ClienteSunat> clientes = sunatService.obtenerClientesRecientes(100);
            
            // Agregamos logs para diagnóstico
            logger.info("Obteniendo resultados de consultas. Cantidad de registros: {}", 
                        clientes != null ? clientes.size() : "null");
            
            model.addAttribute("clientes", clientes);
            
            // Si no hay clientes, agregamos un mensaje informativo
            if (clientes == null || clientes.isEmpty()) {
                model.addAttribute("infoMessage", "No se encontraron registros de consultas en la base de datos.");
            }
            
            return "consulta-sunat/resultados";
        } catch (Exception e) {
            logger.error("Error al cargar los resultados de consultas", e);
            model.addAttribute("errorMessage", "Error al cargar los datos: " + e.getMessage());
            return "consulta-sunat/resultados";
        }
    }
    
    // Página de historial
    @GetMapping("/historial")
    public String mostrarHistorial(Model model) {
        try {
            List<ConsultaSunat> historial = sunatService.obtenerHistorialReciente(100);
            model.addAttribute("historial", historial);
            logger.info("Cargando página de historial con {} registros", historial.size());
        } catch (Exception e) {
            logger.error("Error al cargar historial: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error al cargar el historial: " + e.getMessage());
        }
        return "consulta-sunat/historial";
    }
    
    // Página de consulta masiva
    @GetMapping("/masiva")
    public String mostrarConsultaMasiva() {
        return "consulta-sunat/consulta-masiva";
    }
    


    
    @PostMapping("/consultar")
    public String consultarRuc(@RequestParam("ruc") String ruc, RedirectAttributes redirectAttributes) {
        try {
            ClienteSunat cliente = sunatService.consultarRuc(ruc);
            if (cliente != null) {
                redirectAttributes.addFlashAttribute("successMessage", 
                        "Consulta exitosa para el RUC " + ruc);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "No se encontraron datos para el RUC " + ruc + " en la API de SUNAT");
            }
        } catch (Exception e) {
            logger.error("Error al consultar RUC {}: {}", ruc, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Error al consultar el RUC " + ruc + ": " + e.getMessage());
        }
        return "redirect:/consulta-sunat";
    }
    
    @PostMapping("/consulta-masiva")
    public String consultaMasiva(@RequestParam("archivo") MultipartFile archivo, 
                                RedirectAttributes redirectAttributes) {
        try {
            if (archivo.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "El archivo está vacío");
                return "redirect:/consulta-sunat";
            }
            
            // Leer RUCs del archivo
            List<String> listaRucs = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(archivo.getInputStream()))) {
                listaRucs = reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toList());
            }
            
            if (listaRucs.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "No se encontraron RUCs en el archivo");
                return "redirect:/consulta-sunat";
            }
            
            // Iniciar consulta asíncrona
            sunatService.consultarVariosRucs(listaRucs);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Se ha iniciado la consulta masiva de " + listaRucs.size() + 
                    " RUCs usando la API de SUNAT. Los resultados estarán disponibles en breve.");
            
        } catch (Exception e) {
            logger.error("Error al procesar archivo de consulta masiva: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Error al procesar el archivo: " + e.getMessage());
        }
        return "redirect:/consulta-sunat";
    }

    
    @GetMapping("/cliente/{ruc}")
    @ResponseBody
    public ResponseEntity<ClienteSunat> obtenerCliente(@PathVariable String ruc) {
        try {
            ClienteSunat cliente = sunatService.consultarRuc(ruc);
            if (cliente != null) {
                return ResponseEntity.ok(cliente);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al consultar cliente con RUC {}: {}", ruc, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(null);
        }
    }
    
    @GetMapping("/consultas/{ruc}")
    @ResponseBody
    public ResponseEntity<List<ConsultaSunat>> obtenerConsultas(@PathVariable String ruc) {
        try {
            List<ConsultaSunat> consultas = sunatService.obtenerHistorialPorRuc(ruc);
            if (consultas != null && !consultas.isEmpty()) {
                return ResponseEntity.ok(consultas);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error al obtener consultas para RUC {}: {}", ruc, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(null);
        }
    }

    @GetMapping("/verificar-conexion")
@ResponseBody
public ResponseEntity<Map<String, Object>> verificarConexion() {
    Map<String, Object> respuesta = new HashMap<>();
    boolean conexionDB = sunatService.verificarConexionBD();
    
    respuesta.put("conexionDB", conexionDB);
    respuesta.put("timestamp", new Date());
    
    return ResponseEntity.ok(respuesta);
}
}