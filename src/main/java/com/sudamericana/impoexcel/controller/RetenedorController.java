package com.sudamericana.impoexcel.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sudamericana.impoexcel.model.AgenteRetenedor;
import com.sudamericana.impoexcel.model.ConsultaRetenedor;
import com.sudamericana.impoexcel.service.RetenedorConsultaService;

@Controller
@RequestMapping("/retenedores")
public class RetenedorController {
    
    private static final Logger logger = LoggerFactory.getLogger(RetenedorController.class);
    
    @Autowired
    private RetenedorConsultaService retenedorService;
    
    @GetMapping
    public String mostrarPaginaConsulta(Model model) {
        return "retenedores/retenedor-individual";
    }
    
    // Página de resultados
    @GetMapping("/resultados")
    public String mostrarResultados(Model model) {
        try {
            List<AgenteRetenedor> retenedores = retenedorService.obtenerRetenedoresRecientes(100);
            model.addAttribute("retenedores", retenedores);
            logger.info("Cargando página de resultados con {} retenedores", retenedores.size());
        } catch (Exception e) {
            logger.error("Error al cargar resultados: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error al cargar los resultados: " + e.getMessage());
        }
        return "retenedores/resultados";
    }
    
    // Página de historial
    @GetMapping("/historial")
    public String mostrarHistorial(Model model) {
        try {
            List<ConsultaRetenedor> historial = retenedorService.obtenerHistorialReciente(100);
            model.addAttribute("historial", historial);
            logger.info("Cargando página de historial con {} registros", historial.size());
        } catch (Exception e) {
            logger.error("Error al cargar historial: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error al cargar el historial: " + e.getMessage());
        }
        return "retenedores/historial";
    }
    
    // Página de consulta masiva
    @GetMapping("/masiva")
    public String mostrarConsultaMasiva() {
        return "retenedores/retenedor-masiva";
    }
    
    @PostMapping("/consultar")
    public String consultarRuc(@RequestParam("ruc") String ruc, RedirectAttributes redirectAttributes) {
        try {
            if (ruc == null || ruc.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Debe ingresar un número de RUC");
                return "redirect:/retenedores";
            }
            
            if (ruc.trim().length() != 11 || !ruc.matches("\\d+")) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "El RUC debe tener 11 dígitos numéricos");
                return "redirect:/retenedores";
            }
            
            AgenteRetenedor retenedor = retenedorService.consultarRetenedor(ruc);
            if (retenedor != null) {
                redirectAttributes.addFlashAttribute("successMessage", 
                        "El RUC " + ruc + " es agente retenedor");
            } else {
                redirectAttributes.addFlashAttribute("warningMessage", 
                        "El RUC " + ruc + " no es agente retenedor");
            }
        } catch (Exception e) {
            logger.error("Error al consultar RUC {}: {}", ruc, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Error al consultar el RUC " + ruc + ": " + e.getMessage());
        }
        return "redirect:/retenedores";
    }
    
    @PostMapping("/consulta-masiva")
public String consultaMasiva(@RequestParam("archivo") MultipartFile archivo, 
                            RedirectAttributes redirectAttributes) {
    try {
        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Por favor seleccione un archivo para procesar.");
            return "redirect:/retenedores/masiva";
        }
        
        // Procesar el archivo y extraer los RUCs
        List<String> rucs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String ruc = line.trim();
                if (ruc.matches("\\d{11}")) {
                    rucs.add(ruc);
                }
            }
        }
        
        if (rucs.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "No se encontraron RUCs válidos en el archivo. Asegúrese de que cada línea contenga un RUC de 11 dígitos.");
            return "redirect:/retenedores/masiva";
        }
        
        // Iniciar el proceso asíncrono de consulta
        retenedorService.consultarVariosRucs(rucs);
        
        redirectAttributes.addFlashAttribute("successMessage", 
                "Consulta masiva iniciada. Se procesarán " + rucs.size() + 
                " RUCs individualmente. Consulte la sección de resultados para ver el avance.");
        
        // Añadir parámetro para mostrar indicador de progreso
        redirectAttributes.addAttribute("processingMasivo", true);
        
    } catch (Exception e) {
        logger.error("Error al procesar el archivo de RUCs: {}", e.getMessage(), e);
        redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al procesar el archivo: " + e.getMessage());
        return "redirect:/retenedores/masiva";
    }
    
    return "redirect:/retenedores/resultados";
}
    
    @PostMapping("/consulta-masiva-bd")
public String consultaMasivaDesdeBaseDatos(RedirectAttributes redirectAttributes) {
    try {
        // Obtener la cantidad de RUCs primero para mostrar información
        List<String> rucs = retenedorService.obtenerRucsDesdeStoredProcedure();
        
        if (rucs.isEmpty()) {
            redirectAttributes.addFlashAttribute("warningMessage", 
                    "No se encontraron RUCs válidos en la base de datos para procesar.");
            return "redirect:/retenedores/masiva";
        }
        
        // Iniciar consulta asíncrona desde BD
        retenedorService.consultarRetenedoresDesdeBD();
        
        redirectAttributes.addFlashAttribute("successMessage", 
                "Consulta masiva iniciada con " + rucs.size() + 
                " RUCs de la base de datos. Cada RUC será procesado individualmente. " +
                "Consulte la sección de resultados para ver el avance.");
        
        // Añadir parámetro para mostrar indicador de progreso
        redirectAttributes.addAttribute("processingMasivo", true);
        
    } catch (Exception e) {
        logger.error("Error al iniciar la consulta masiva desde BD: {}", e.getMessage(), e);
        redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al iniciar la consulta masiva: " + e.getMessage());
        return "redirect:/retenedores/masiva";
    }
    
    return "redirect:/retenedores/resultados";
}

    @GetMapping("/retenedor/{ruc}")
    @ResponseBody
    public ResponseEntity<AgenteRetenedor> obtenerRetenedor(@PathVariable String ruc) {
        AgenteRetenedor retenedor = retenedorService.obtenerRetenedorPorRuc(ruc);
        if (retenedor != null) {
            return ResponseEntity.ok(retenedor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/consultas/{ruc}")
    @ResponseBody
    public ResponseEntity<List<ConsultaRetenedor>> obtenerConsultas(@PathVariable String ruc) {
        List<ConsultaRetenedor> consultas = retenedorService.obtenerHistorialPorRuc(ruc);
        return ResponseEntity.ok(consultas);
    }
    
    @GetMapping("/verificar/{ruc}")
    @ResponseBody
    public ResponseEntity<Boolean> verificarRetenedor(@PathVariable String ruc) {
        boolean esRetenedor = retenedorService.esRetenedor(ruc);
        return ResponseEntity.ok(esRetenedor);
    }
}