package com.sudamericana.impoexcel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sudamericana.impoexcel.service.ExportarPreciosOCService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/exportar-precios-oc")
public class ExportarPreciosOCController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExportarPreciosOCController.class);

    @Autowired
    private ExportarPreciosOCService exportService;

    @GetMapping
    public String mostrarFormulario() {
        return "exportar-precios-oc/form";
    }

    @PostMapping("/exportar")
    public String exportarExcel(@RequestParam("numeroOC") String numeroOC,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {
        try {
            if (!numeroOC.matches("\\d{3}-\\d{8}")) {
                throw new IllegalArgumentException("Formato de OC inválido. Use el formato: 001-00034342");
            }
    
            response.setContentType("application/vnd.ms-excel.sheet.macroEnabled.12");
            response.setHeader("Content-Disposition", "attachment; filename=ListaPrecios_OC_" + numeroOC + ".xlsm");
            
            exportService.exportarAExcel(numeroOC, response.getOutputStream());
            return null;
            
        } catch (Exception e) {
            logger.error("Error al exportar Excel para OC " + numeroOC, e);
            redirectAttributes.addFlashAttribute("error", "Error al exportar: " + e.getMessage());
            return "redirect:/exportar-precios-oc";
        }
    }
}