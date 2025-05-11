package com.sudamericana.impoexcel.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ListadoTransitoService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    public byte[] generarReporteExcel() throws Exception {
        List<Map<String, Object>> datos = getListadoTransito();
        
        // Identificar todos los proveedores disponibles en los datos
        List<String> proveedores = identificarProveedores(datos);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Listado en Tránsito");

            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numericStyle = createNumericStyle(workbook, "#,##0.0000");
            CellStyle cantidadStyle = createNumericStyle(workbook, "#,##0.0000");

            // Crear encabezado del reporte
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("LISTADO DE MERCADERÍA EN TRÁNSITO");
            titleCell.setCellStyle(headerStyle);
            // Ajustar el merge para incluir todas las columnas (7 columnas base + proveedores)
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7 + proveedores.size() - 1));

            // Fecha generación
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Generado: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7 + proveedores.size() - 1));

            // Encabezados de columnas
            Row headerRow = sheet.createRow(2);
            String[] baseHeaders = { "Código", "Código Final", "Marca", "Descripción", "Unidad Medida",
                    "Precio Unitario", "Precio Total" };

            // Agregar encabezados base
            for (int i = 0; i < baseHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(baseHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Agregar encabezados de proveedores
            for (int i = 0; i < proveedores.size(); i++) {
                Cell cell = headerRow.createCell(baseHeaders.length + i);
                cell.setCellValue(proveedores.get(i).trim());
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 3;
            Map<String, Double> totalesPorProveedor = new HashMap<>();
            double totalPrecio = 0;

            for (Map<String, Object> dato : datos) {
                Row row = sheet.createRow(rowNum++);

                // Columnas de texto
                row.createCell(0).setCellValue(getString(dato.get("codi")));
                row.createCell(1).setCellValue(getString(dato.get("codf")));
                row.createCell(2).setCellValue(getString(dato.get("marc")));
                row.createCell(3).setCellValue(getString(dato.get("descr")));
                row.createCell(4).setCellValue(getString(dato.get("umed")));

                // Columnas numéricas de precio
                Cell precioUnitCell = row.createCell(5);
                precioUnitCell.setCellValue(getDouble(dato.get("preu")));
                precioUnitCell.setCellStyle(numericStyle);

                Cell precioTotalCell = row.createCell(6);
                precioTotalCell.setCellValue(getDouble(dato.get("tota")));
                precioTotalCell.setCellStyle(numericStyle);
                totalPrecio += getDouble(dato.get("tota"));

                // Columnas de cantidades por proveedor
                for (int i = 0; i < proveedores.size(); i++) {
                    String proveedor = proveedores.get(i);
                    Cell cantidadCell = row.createCell(7 + i);
                    
                    double cantidad = getDouble(dato.get(proveedor));
                    cantidadCell.setCellValue(cantidad);
                    cantidadCell.setCellStyle(cantidadStyle);
                    
                    // Acumular totales por proveedor
                    totalesPorProveedor.put(proveedor, 
                                           totalesPorProveedor.getOrDefault(proveedor, 0.0) + cantidad);
                }
            }

            // Fila de totales
            if (!datos.isEmpty()) {
                Row totalRow = sheet.createRow(rowNum);
                Cell labelCell = totalRow.createCell(3);
                labelCell.setCellValue("TOTALES:");
                labelCell.setCellStyle(headerStyle);

                // Total de precio
                Cell totalPrecioCell = totalRow.createCell(6);
                totalPrecioCell.setCellValue(totalPrecio);
                totalPrecioCell.setCellStyle(numericStyle);

                // Totales por proveedor
                for (int i = 0; i < proveedores.size(); i++) {
                    String proveedor = proveedores.get(i);
                    Cell totalProvCell = totalRow.createCell(7 + i);
                    totalProvCell.setCellValue(totalesPorProveedor.getOrDefault(proveedor, 0.0));
                    totalProvCell.setCellStyle(cantidadStyle);
                }
            }

            // Ajustar anchos de columna
            for (int i = 0; i < baseHeaders.length + proveedores.size(); i++) {
                sheet.autoSizeColumn(i);
                // Ancho mínimo
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
                // Limitar ancho máximo para descripción y nombres de proveedor
                if ((i == 3 || i >= baseHeaders.length) && sheet.getColumnWidth(i) > 12000) {
                    sheet.setColumnWidth(i, 12000);
                }
            }

            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private List<String> identificarProveedores(List<Map<String, Object>> datos) {
        List<String> proveedores = new ArrayList<>();
        
        if (!datos.isEmpty()) {
            Map<String, Object> firstRow = datos.get(0);
            
            for (String key : firstRow.keySet()) {
                // Excluir columnas base
                if (!key.equals("codi") && !key.equals("codf") && 
                    !key.equals("marc") && !key.equals("descr") && 
                    !key.equals("umed") && !key.equals("preu") && 
                    !key.equals("tota")) {
                    proveedores.add(key);
                }
            }
        }
        
        return proveedores;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createNumericStyle(Workbook workbook, String format) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat(format));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    public List<Map<String, Object>> getListadoTransito() {
        try {
            String sql = "EXEC SP_ReporteTransitoBoton_sw";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            System.err.println("Error al obtener datos de tránsito: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<String> getProveedores() {
        return identificarProveedores(getListadoTransito());
    }

    private String getString(Object value) {
        return value != null ? value.toString() : "";
    }

    private double getDouble(Object value) {
        if (value == null)
            return 0.0;
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
