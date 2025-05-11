package com.sudamericana.impoexcel.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class RetenedoresService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ResourceLoader resourceLoader;

    private static final String TEMPLATE_PATH = "classpath:templates/plantillas/PlantiSaldosClienteRetenedor.xlsx";
    
    public byte[] generarReporteExcel() throws Exception {
        List<Map<String, Object>> datos = getRetenedoresConSaldo();
        
        var resource = resourceLoader.getResource(TEMPLATE_PATH);
        try (FileInputStream fis = new FileInputStream(resource.getFile());
             Workbook workbook = new XSSFWorkbook(fis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            titleCell.setCellValue("Reporte de Clientes Agentes Retenedores Con Saldo Activo a la fecha y hora: " + currentDate);
            titleStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleCell.setCellStyle(titleStyle);
            
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 14));
    
            int rowNum = 2;
            
            for (Map<String, Object> dato : datos) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(getString(dato.get("codcli")));     // Columna A
                row.createCell(1).setCellValue(getString(dato.get("nomcli")));     // Columna B
                row.createCell(2).setCellValue(getString(dato.get("dircli")));     // Columna C
                row.createCell(3).setCellValue(getString(dato.get("ruccli")));     // Columna D
                row.createCell(4).setCellValue(getString(dato.get("telcli")));     // Columna E
                row.createCell(5).setCellValue(getString(dato.get("faxcli")));     // Columna F
                row.createCell(6).setCellValue(getString(dato.get("email")));      // Columna G
                row.createCell(7).setCellValue(getString(dato.get("NomZon")));     // Columna H
                row.createCell(8).setCellValue(getString(dato.get("NomAct")));     // Columna I
                row.createCell(9).setCellValue(getString(dato.get("NomVen")));     // Columna J
                row.createCell(10).setCellValue(getString(dato.get("NomCob")));    // Columna K
                row.createCell(11).setCellValue(getDouble(dato.get("mcredi")));    // Columna L
                row.createCell(12).setCellValue(getString(dato.get("NomPro")));    // Columna M
                row.createCell(13).setCellValue("1".equals(getString(dato.get("ArSnt"))) ? "Si" : "No"); // Columna N
                row.createCell(14).setCellValue(getDouble(dato.get("TotalSaldo"))); // Columna O
            }
            
            for (int i = 0; i < 15; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public List<Map<String, Object>> getRetenedoresConSaldo() {
        String sql = "EXEC sp_GetClientesConDeudaYAgenteRetenedor_sw";
        return jdbcTemplate.queryForList(sql);
    }
private String getString(Object value) {
    return value != null ? value.toString() : "";
}

private double getDouble(Object value) {
    if (value == null) return 0.0;
    try {
        return Double.parseDouble(value.toString());
    } catch (NumberFormatException e) {
        return 0.0;
    }
}
}