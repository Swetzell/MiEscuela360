package com.sudamericana.impoexcel.controller;

import com.sudamericana.impoexcel.model.Marca;
import com.sudamericana.impoexcel.model.ReporteABC;
import com.sudamericana.impoexcel.model.ReporteABCFiltro;
import com.sudamericana.impoexcel.model.Vendedor;
import com.sudamericana.impoexcel.service.MarcaService;
import com.sudamericana.impoexcel.service.ReporteABCService;
import com.sudamericana.impoexcel.service.VendedorServiceImpl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/reportesabc")
public class ReporteABCController {

    private static final Logger logger = LoggerFactory.getLogger(ReporteABCController.class);
    private final ReporteABCService reporteABCService;
    private final VendedorServiceImpl vendedorServiceImpl;
    private final MarcaService marcaService;

    @Autowired
    public ReporteABCController(ReporteABCService reporteABCService, 
        VendedorServiceImpl vendedorServiceImpl,
        MarcaService marcaService) {
        this.reporteABCService = reporteABCService;
        this.vendedorServiceImpl = vendedorServiceImpl;
        this.marcaService = marcaService;
    }

    @GetMapping
    public String mostrarFormulario(Model model) {
        try {
            ReporteABCFiltro filtro = new ReporteABCFiltro();
            
            filtro.setFechaInicio(LocalDate.now().withDayOfMonth(1));
            filtro.setFechaFin(LocalDate.now());
    
            List<Vendedor> vendedores = vendedorServiceImpl.listarTodos();
            
            List<Marca> marcas = marcaService.listarTodas();
            
            boolean hayTodos = false;
            for (Marca m : marcas) {
                if ("%".equals(m.getId()) || "0".equals(m.getId())) {
                    hayTodos = true;
                    break;
                }
            }
            
            if (!hayTodos && !marcas.isEmpty()) {
                marcas.add(0, new Marca("%", "[ Todos ]"));
            }
            
            logger.info("Formulario cargado con {} marcas", marcas.size());
            
            model.addAttribute("filtro", filtro);
            model.addAttribute("vendedores", vendedores);
            model.addAttribute("marcas", marcas);
            
            return "reportesabc";
            
        } catch (Exception e) {
            logger.error("Error al mostrar el formulario de reportes ABC", e);
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/exportar")
    public void exportarExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false, defaultValue = "%") String vendedor,
            @RequestParam(required = false, defaultValue = "%") String estado,
            @RequestParam(required = false, defaultValue = "0") String duvDesde,
            @RequestParam(required = false, defaultValue = "10000") String duvHasta,
            @RequestParam(required = false, defaultValue = "%") String condicionPago,
            @RequestParam(required = false, defaultValue = "1") String tipoReporte,
            @RequestParam(required = false, defaultValue = "EXCEL") String formato,
            // reporte de productos
            @RequestParam(required = false, defaultValue = "%") String marca,
            @RequestParam(required = false, defaultValue = "20") String pctCantidad,
            @RequestParam(required = false, defaultValue = "40") String pctMovimiento,
            @RequestParam(required = false, defaultValue = "40") String pctVenta,
            @RequestParam(required = false, defaultValue = "50") String curvaA,
            @RequestParam(required = false, defaultValue = "65") String curvaB,
            @RequestParam(required = false, defaultValue = "100") String curvaC,
            HttpServletResponse response) throws IOException {
        
        try {
            ReporteABCFiltro filtro = new ReporteABCFiltro();
            
            if (fechaInicio != null && !fechaInicio.isEmpty()) {
                filtro.setFechaInicio(LocalDate.parse(fechaInicio));
            }
            
            if (fechaFin != null && !fechaFin.isEmpty()) {
                filtro.setFechaFin(LocalDate.parse(fechaFin));
            }
            
            filtro.setVendedor(vendedor);
            filtro.setEstado(estado);
            filtro.setDuvDesde(duvDesde);
            filtro.setDuvHasta(duvHasta);
            filtro.setCondicionPago(condicionPago);
            filtro.setTipoReporte(tipoReporte);
            filtro.setFormato(formato);
            
            if ("2".equals(tipoReporte)) {
                filtro.setMarca(marca);
                filtro.setPctCantidad(pctCantidad);
                filtro.setPctMovimiento(pctMovimiento);
                filtro.setPctVenta(pctVenta);
                filtro.setCurvaA(curvaA);
                filtro.setCurvaB(curvaB);
                filtro.setCurvaC(curvaC);
            }
            
            List<ReporteABC> reportes;
            if ("2".equals(tipoReporte)) {
                reportes = reporteABCService.generarReporteABCProducto(filtro);
                logger.info("Generando reporte ABC de productos");
            } else {
                reportes = reporteABCService.generarReporteABC(filtro);
                logger.info("Generando reporte ABC de clientes");
            }
        
            String nombreReporte = "2".equals(tipoReporte) ? "ReporteABC_Productos" : "ReporteABC_Clientes";
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + nombreReporte + "_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");
            
            generarExcelReporte(reportes, tipoReporte, response);
            
        } catch (Exception e) {
            logger.error("Error al exportar el reporte ABC", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al exportar el reporte: " + e.getMessage());
        }
    }

    
private void generarExcelReporte(List<ReporteABC> reportes, String tipoReporte, HttpServletResponse response) throws IOException {
    String nombreReporte = "2".equals(tipoReporte) ? "ReporteABC_Productos" : "ReporteABC_Clientes";
    
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=" + nombreReporte + "_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");
    
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("ReporteABC");
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle curvaAStyle = workbook.createCellStyle();
        curvaAStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        curvaAStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle curvaBStyle = workbook.createCellStyle();
        curvaBStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        curvaBStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle curvaCStyle = workbook.createCellStyle();
        curvaCStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        curvaCStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        DataFormat format = workbook.createDataFormat();
        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(format.getFormat("#,##0.00"));
        
        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(format.getFormat("#,##0"));
        
        if ("2".equals(tipoReporte)) {
            
            Row headerRow = sheet.createRow(0);
            int colIdx = 0;
            
            String[] basicHeaders = {"CURVA", "CODIGO", "MARCA", "DESCRIPCIÓN", "STOCK", "TOTAL"};
            for (String header : basicHeaders) {
                Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(header);
                cell.setCellStyle(headerStyle);
            }
            
            String[] meses = {"ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SET", "OCT", "NOV", "DIC"};
            for (String mes : meses) {
                Cell cell = headerRow.createCell(colIdx);
                cell.setCellValue(mes);
                cell.setCellStyle(headerStyle);
            
                sheet.addMergedRegion(new CellRangeAddress(0, 0, colIdx, colIdx + 1));
                colIdx += 2;
            }
            
            Cell pctCell = headerRow.createCell(colIdx);
            pctCell.setCellValue("%");
            pctCell.setCellStyle(headerStyle);
            
            Row subHeaderRow = sheet.createRow(1);
            
            for (int i = 0; i < basicHeaders.length; i++) {
                subHeaderRow.createCell(i);
            }
            
            colIdx = basicHeaders.length;
            for (int i = 0; i < meses.length; i++) {
                Cell cCell = subHeaderRow.createCell(colIdx++);
                cCell.setCellValue("C");
                cCell.setCellStyle(headerStyle);
                
                Cell mCell = subHeaderRow.createCell(colIdx++);
                mCell.setCellValue("M");
                mCell.setCellStyle(headerStyle);
            }
            
            subHeaderRow.createCell(colIdx);
            
            // Datos de productos
            int rowIdx = 2;
            for (ReporteABC reporte : reportes) {
                Row dataRow = sheet.createRow(rowIdx++);
                colIdx = 0;
                
                CellStyle curvaStyle;
                if ("A".equals(reporte.getCurva())) {
                    curvaStyle = curvaAStyle;
                } else if ("B".equals(reporte.getCurva())) {
                    curvaStyle = curvaBStyle;
                } else {
                    curvaStyle = curvaCStyle;
                }
                
                // Datos básicos
                Cell curvaCell = dataRow.createCell(colIdx++);
                curvaCell.setCellValue(reporte.getCurva() != null ? reporte.getCurva() : "");
                curvaCell.setCellStyle(curvaStyle);
                
                dataRow.createCell(colIdx++).setCellValue(reporte.getCodProducto() != null ? reporte.getCodProducto() : "");
                dataRow.createCell(colIdx++).setCellValue(reporte.getMarca() != null ? reporte.getMarca() : "");
                dataRow.createCell(colIdx++).setCellValue(reporte.getProducto() != null ? reporte.getProducto() : "");
                
                // Stock
                Cell stockCell = dataRow.createCell(colIdx++);
                stockCell.setCellValue(reporte.getStock() != null ? reporte.getStock().doubleValue() : 0);
                stockCell.setCellStyle(numberStyle);
                
                // Total cantidad
                Cell totalCell = dataRow.createCell(colIdx++);
                totalCell.setCellValue(reporte.getCantidad() != null ? reporte.getCantidad().doubleValue() : 0);
                totalCell.setCellStyle(numberStyle);
                
                // Datos mensuales
                if (reporte.getCantidadEne() != null) {
                    // Enero
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadEne(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoEne(), moneyStyle);
                    
                    // Febrero
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadFeb(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoFeb(), moneyStyle);
                    
                    // Marzo
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadMar(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoMar(), moneyStyle);
                    
                    // Abril
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadAbr(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoAbr(), moneyStyle);
                    
                    // Mayo
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadMay(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoMay(), moneyStyle);
                    
                    // Junio
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadJun(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoJun(), moneyStyle);
                    
                    // Julio
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadJul(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoJul(), moneyStyle);
                    
                    // Agosto
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadAgo(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoAgo(), moneyStyle);
                    
                    // Septiembre
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadSet(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoSet(), moneyStyle);
                    
                    // Octubre
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadOct(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoOct(), moneyStyle);
                    
                    // Noviembre
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadNov(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoNov(), moneyStyle);
                    
                    // Diciembre
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getCantidadDic(), numberStyle);
                    agregarCeldaMensual(dataRow, colIdx++, reporte.getMontoDic(), moneyStyle);
                } else {
                    for (int i = 0; i < 24; i++) {
                        dataRow.createCell(colIdx++);
                    }
                }
                dataRow.createCell(colIdx).setCellValue(reporte.getPorcAcumulada() != null ? reporte.getPorcAcumulada() : "0");
            }
        } else {
            Row headerRow = sheet.createRow(0);
            
            String[] headers = {
                "Curva", "Código Cliente", "Cliente", "RUC", "D.U.V.", 
                "Cantidad", "Monto", "% Acum.", "Vendedor", "Estado", 
                "Línea de Crédito", "Aprobación de Crédito", "Condición de Pago",
                "Departamento", "Distrito", "Teléfono"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (ReporteABC reporte : reportes) {
                Row row = sheet.createRow(rowNum++);
                
                CellStyle rowStyle;
                if ("A".equals(reporte.getCurva())) {
                    rowStyle = curvaAStyle;
                } else if ("B".equals(reporte.getCurva())) {
                    rowStyle = curvaBStyle;
                } else {
                    rowStyle = curvaCStyle;
                }
                
                Cell curvaCell = row.createCell(0);
                curvaCell.setCellValue(reporte.getCurva() != null ? reporte.getCurva() : "N/A");
                curvaCell.setCellStyle(rowStyle);
                
                int col = 1;
                row.createCell(col++).setCellValue(reporte.getCodCli() != null ? reporte.getCodCli() : "");
                row.createCell(col++).setCellValue(reporte.getCliente() != null ? reporte.getCliente() : "");
                row.createCell(col++).setCellValue(reporte.getRuccli() != null ? reporte.getRuccli() : "");
                row.createCell(col++).setCellValue(reporte.getDiaz() != null ? reporte.getDiaz() : 0);
                
                Cell cantidadCell = row.createCell(col++);
                cantidadCell.setCellValue(reporte.getCantidad() != null ? reporte.getCantidad().doubleValue() : 0);
                cantidadCell.setCellStyle(numberStyle);
                
                Cell montoCell = row.createCell(col++);
                montoCell.setCellValue(reporte.getMonto() != null ? reporte.getMonto().doubleValue() : 0);
                montoCell.setCellStyle(moneyStyle);
                
                row.createCell(col++).setCellValue(reporte.getPorcAcumulada() != null ? reporte.getPorcAcumulada() : "0");
                row.createCell(col++).setCellValue(reporte.getDesvendedor() != null ? reporte.getDesvendedor() : "");
                row.createCell(col++).setCellValue(reporte.getDesestado() != null ? reporte.getDesestado() : "");
                
                Cell limiteCreditoCell = row.createCell(col++);
                limiteCreditoCell.setCellValue(reporte.getLimiteCredito() != null ? reporte.getLimiteCredito().doubleValue() : 0);
                limiteCreditoCell.setCellStyle(moneyStyle);
                
                row.createCell(col++).setCellValue(reporte.getAprobacion() != null ? reporte.getAprobacion() : "");
                row.createCell(col++).setCellValue(reporte.getCondicionPago() != null ? reporte.getCondicionPago() : "");
                row.createCell(col++).setCellValue(reporte.getDesdepartamento() != null ? reporte.getDesdepartamento() : "");
                row.createCell(col++).setCellValue(reporte.getDistrito() != null ? reporte.getDistrito() : "");
                row.createCell(col++).setCellValue(reporte.getTelefono() != null ? reporte.getTelefono() : "");
            }
        }
        
        int lastCol = sheet.getRow(0).getLastCellNum();
        for (int i = 0; i < lastCol; i++) {
            sheet.autoSizeColumn(i);
        }
        
        workbook.write(response.getOutputStream());
        logger.info("Reporte ABC generado con éxito - Tipo: {}, Registros: {}", 
                tipoReporte.equals("2") ? "Productos" : "Clientes", reportes.size());
    }
}

private void agregarCeldaMensual(Row row, int colIndex, BigDecimal valor, CellStyle estilo) {
    Cell cell = row.createCell(colIndex);
    if (valor != null && valor.compareTo(BigDecimal.ZERO) != 0) {
        cell.setCellValue(valor.doubleValue());
        cell.setCellStyle(estilo);
    } else {
        cell.setCellValue(0);
        cell.setCellStyle(estilo);
    }
}

    @PostMapping("/generar")
    public String generarReporte(@ModelAttribute ReporteABCFiltro filtro, Model model, RedirectAttributes redirectAttributes) {
        return "redirect:/reportesabc/exportar?" + 
               "fechaInicio=" + filtro.getFechaInicio() + 
               "&fechaFin=" + filtro.getFechaFin() +
               "&vendedor=" + filtro.getVendedor() +
               "&estado=" + filtro.getEstado() +
               "&duvDesde=" + filtro.getDuvDesde() +
               "&duvHasta=" + filtro.getDuvHasta() +
               "&condicionPago=" + filtro.getCondicionPago() +
               "&tipoReporte=" + filtro.getTipoReporte();
    }
}