package com.miescuela360.controller;

import com.miescuela360.model.Alumno;
import com.miescuela360.model.Pago;
import com.miescuela360.service.AlumnoService;
import com.miescuela360.service.GradoService;
import com.miescuela360.service.PagoService;
import com.miescuela360.service.SeccionService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private PagoService pagoService;
    
    @Autowired
    private AlumnoService alumnoService;
    
    @Autowired
    private GradoService gradoService;
    
    @Autowired
    private SeccionService seccionService;

    @GetMapping
    public String mostrarReportes(Model model) {
        model.addAttribute("estados", pagoService.obtenerTodosLosEstados());
        model.addAttribute("grados", gradoService.findAll());
        model.addAttribute("secciones", seccionService.findAll());
        return "reportes/index";
    }

    @GetMapping("/pagos/excel")
    public ResponseEntity<byte[]> generarReportePagos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin) {
        
        try {
            // Obtener pagos filtrados
            List<Pago> pagos = obtenerPagosFiltrados(estado, fechaInicio, fechaFin);
            
            // Crear el libro de Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Reporte de Pagos");
            
            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            String[] headers = {"ID", "Alumno", "Fecha", "Monto", "Estado", "Tipo de Pago", "Fecha de Pago"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            int rowNum = 1;
            for (Pago pago : pagos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(pago.getId());
                row.createCell(1).setCellValue(pago.getAlumno().getNombre() + " " + pago.getAlumno().getApellido());
                row.createCell(2).setCellValue(pago.getFecha().toString());
                row.createCell(3).setCellValue(pago.getMonto().doubleValue());
                row.createCell(4).setCellValue(pago.getEstado().toString());
                row.createCell(5).setCellValue(pago.getTipo().toString());
                
                if (pago.getFechaPago() != null) {
                    row.createCell(6).setCellValue(pago.getFechaPago().toString());
                }
            }
            
            // Ajustar el ancho de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Generar el archivo Excel
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            // Crear la respuesta
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String filename = "reporte_pagos_" + LocalDate.now() + ".xlsx";
            responseHeaders.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(outputStream.toByteArray(), responseHeaders, HttpStatus.OK);
            
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
        
    @GetMapping("/alumnos/excel")
    public ResponseEntity<byte[]> generarReporteAlumnos(
            @RequestParam(required = false) Long gradoId,
            @RequestParam(required = false) Long seccionId,
            @RequestParam(required = false) String estado) {
        
        try {
            // Obtener alumnos filtrados
            List<Alumno> alumnos = obtenerAlumnosFiltrados(gradoId, seccionId, estado);
            
            // Crear el libro de Excel
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Reporte de Alumnos");
            
            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            String[] headers = {"ID", "DNI", "Nombre", "Apellido", "Grado", "Sección", "Fecha Nacimiento", "Estado"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Llenar datos
            int rowNum = 1;
            for (Alumno alumno : alumnos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(alumno.getId());
                row.createCell(1).setCellValue(alumno.getDni());
                row.createCell(2).setCellValue(alumno.getNombre());
                row.createCell(3).setCellValue(alumno.getApellido());
                
                if (alumno.getGrado() != null) {
                    row.createCell(4).setCellValue(alumno.getGrado().getNombre());
                } else {
                    row.createCell(4).setCellValue("No asignado");
                }
                
                if (alumno.getSeccion() != null) {
                    row.createCell(5).setCellValue(alumno.getSeccion().getNombre());
                } else {
                    row.createCell(5).setCellValue("No asignada");
                }
                
                row.createCell(6).setCellValue(alumno.getFechaNacimiento().toString());
                row.createCell(8).setCellValue(alumno.isActivo() ? "ACTIVO" : "INACTIVO");
            }
            
            // Ajustar el ancho de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Generar el archivo Excel
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            // Crear la respuesta
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String filename = "reporte_alumnos_" + LocalDate.now() + ".xlsx";
            responseHeaders.setContentDispositionFormData("attachment", filename);
            
            return new ResponseEntity<>(outputStream.toByteArray(), responseHeaders, HttpStatus.OK);
            
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Pago> obtenerPagosFiltrados(String estado, LocalDate fechaInicio, LocalDate fechaFin) {
        Pago.EstadoPago estadoFiltro = null;
        if (estado != null && !estado.isEmpty()) {
            try {
                estadoFiltro = Pago.EstadoPago.valueOf(estado);
            } catch (IllegalArgumentException e) {
                // Ignorar error si el estado no es válido
            }
        }
        
        if (fechaInicio != null && fechaFin != null) {
            return pagoService.filtrarPagos(fechaInicio, fechaFin, estadoFiltro);
        } else if (estadoFiltro != null) {
            return pagoService.obtenerPagosPorEstado(estadoFiltro);
        } else {
            return pagoService.findAll();
        }
    }
    
    private List<Alumno> obtenerAlumnosFiltrados(Long gradoId, Long seccionId, String estado) {
        // Implementar la lógica de filtrado según los parámetros recibidos
        Boolean estadoActivo = null;
        if (estado != null && !estado.isEmpty()) {
            if (estado.equals("ACTIVO")) {
                estadoActivo = true;
            } else if (estado.equals("INACTIVO")) {
                estadoActivo = false;
            }
        }
        
        if (gradoId != null && seccionId != null) {
            return alumnoService.findByGradoAndSeccion(gradoId, seccionId, estadoActivo);
        } else if (gradoId != null) {
            return alumnoService.findByGrado(gradoId, estadoActivo);
        } else if (seccionId != null) {
            return alumnoService.findBySeccion(seccionId, estadoActivo);
        } else if (estadoActivo != null) {
            return alumnoService.findByActivo(estadoActivo);
        } else {
            return alumnoService.findAll();
        }
    }
}