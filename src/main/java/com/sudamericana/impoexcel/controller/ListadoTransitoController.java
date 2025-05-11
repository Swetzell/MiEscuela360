package com.sudamericana.impoexcel.controller;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sudamericana.impoexcel.service.ListadoTransitoService;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/listado-transito")
public class ListadoTransitoController {

@Autowired
private ListadoTransitoService listadoTransitoService;

@GetMapping
public String mostrarPaginaListadoTransito() {
    return "transito/index";
}
@GetMapping("/datos")
@ResponseBody
public List<Map<String, Object>> obtenerDatosTransito() {
    try {
        return listadoTransitoService.getListadoTransito();
    } catch (Exception e) {
        e.printStackTrace();
        return java.util.Collections.emptyList();
    }
}

@GetMapping("/generar-excel")
public ResponseEntity<byte[]> generarExcelReporte() {
    try {
        byte[] excelBytes = listadoTransitoService.generarReporteExcel();
        if (excelBytes == null || excelBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String filename = "ListadoDeTransito_" + timestamp + ".xlsx";
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelBytes.length)
                .body(excelBytes);
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Error en controlador al generar Excel: " + e.getMessage());
        
        try {
            try (Workbook workbook = new XSSFWorkbook(); 
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                
                Sheet sheet = workbook.createSheet("Error");
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(0);
                cell.setCellValue("Error al generar el reporte: " + e.getMessage());
                
                Row row2 = sheet.createRow(1);
                Cell cell2 = row2.createCell(0);
                cell2.setCellValue("Por favor contacte al administrador del sistema.");
                
                sheet.autoSizeColumn(0);
                
                workbook.write(bos);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String filename = "Error_" + timestamp + ".xlsx";
                headers.setContentDispositionFormData("attachment", filename);
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(bos.size())
                        .body(bos.toByteArray());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return ResponseEntity.internalServerError().build();
    }
}
@GetMapping("/proveedores")
@ResponseBody
public List<String> obtenerProveedores() {
    try {
        return listadoTransitoService.getProveedores();
    } catch (Exception e) {
        e.printStackTrace();
        return java.util.Collections.emptyList();
    }
}
}
