package com.sudamericana.impoexcel.controller;

import com.sudamericana.impoexcel.model.CotizacionPendiente;
import com.sudamericana.impoexcel.model.CotizacionPendienteFiltro;
import com.sudamericana.impoexcel.service.CotizacionPendienteService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/cotizaciones")
public class CotizacionPendienteController {

    private final CotizacionPendienteService cotizacionService;
    private static final Logger logger = LoggerFactory.getLogger(CotizacionPendienteController.class);

    @Autowired
    public CotizacionPendienteController(CotizacionPendienteService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @GetMapping("")
    public String redirigirAPendientes() {
        return "redirect:/cotizaciones/pendientes";
    }

    @GetMapping("/pendientes")
    public String mostrarFormulario(Model model) {
        model.addAttribute("filtro", new CotizacionPendienteFiltro());
        return "cotizaciones/pendientes";
    }

    @PostMapping("/exportar")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute("filtro") CotizacionPendienteFiltro filtro) {
        try {
            logger.info("Exportando cotizaciones pendientes: {} a {}, almacén: {}",
                    filtro.getFechaInicio(), filtro.getFechaFin(),
                    filtro.getCodAlm().isEmpty() ? "TODOS" : filtro.getCodAlm());

            List<CotizacionPendiente> cotizaciones = cotizacionService.buscarCotizacionesPendientes(filtro);
            logger.info("Se encontraron {} cotizaciones para exportar", cotizaciones.size());

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Cotizaciones Pendientes");

                CellStyle titleStyle = createTitleStyle(workbook);
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
                CellStyle dateStyle = createDateStyle(workbook);
                CellStyle numberStyle = createNumberStyle(workbook);
                CellStyle currencyStyle = createCurrencyStyle(workbook);
                CellStyle textStyle = createTextStyle(workbook);
                CellStyle highlightStyle = createHighlightStyle(workbook);

                Row titleRow = sheet.createRow(0);
                titleRow.setHeight((short) 800);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("REPORTE DE COTIZACIONES PENDIENTES DE ATENCIÓN");
                titleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 19));

                Row infoRow = sheet.createRow(1);
                infoRow.setHeight((short) 500);

                Cell fechaDesdeCell = infoRow.createCell(0);
                fechaDesdeCell.setCellValue("Fecha Desde:");
                fechaDesdeCell.setCellStyle(subHeaderStyle);

                Cell fechaDesdeValorCell = infoRow.createCell(1);
                fechaDesdeValorCell
                        .setCellValue(filtro.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                fechaDesdeValorCell.setCellStyle(textStyle);

                Cell fechaHastaCell = infoRow.createCell(3);
                fechaHastaCell.setCellValue("Fecha Hasta:");
                fechaHastaCell.setCellStyle(subHeaderStyle);

                Cell fechaHastaValorCell = infoRow.createCell(4);
                fechaHastaValorCell
                        .setCellValue(filtro.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                fechaHastaValorCell.setCellStyle(textStyle);

                Cell almacenCell = infoRow.createCell(6);
                almacenCell.setCellValue("Almacén:");
                almacenCell.setCellStyle(subHeaderStyle);

                Cell almacenValorCell = infoRow.createCell(7);
                almacenValorCell.setCellValue(filtro.getCodAlm().isEmpty() ? "TODOS" : filtro.getCodAlm());
                almacenValorCell.setCellStyle(textStyle);

                Row spacerRow = sheet.createRow(2);
                spacerRow.setHeight((short) 300);

                Row headerRow = sheet.createRow(3);
                headerRow.setHeight((short) 600);

                String[] headers = {
                        "Código", "Familia", "Marca", "Stock", "Stock a\nFecha Cot.", "Cantidad\nCotización",
                        "Mon", "Precio\nDólares", "Código\nCliente", "Cliente",
                        "Nro\nCotización", "Fecha\nCotización", "Vendedor", "Orden",
                        "Tipo OC", "Proveedor", "Precio\nOrden $", "Nota\nIngreso",
                        "Fecha Ing.", "CPU"
                };

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);

                    if (i == 8 || i == 14) {
                        sheet.setColumnWidth(i, 30 * 256);
                    } else if (i == 2 || i == 11) {
                        sheet.setColumnWidth(i, 20 * 256);
                    } else if (i == 5) {
                        sheet.setColumnWidth(i, 7 * 256);
                    } else {
                        sheet.setColumnWidth(i, 15 * 256);
                    }
                }

                int rowNum = 4;
                boolean alternarColor = false;

                for (CotizacionPendiente cotizacion : cotizaciones) {
                    Row row = sheet.createRow(rowNum++);
                    CellStyle rowStyle = alternarColor ? createAlternateRowStyle(workbook) : textStyle;
                    alternarColor = !alternarColor;

                    int colNum = 0;

                    createCell(row, colNum++, cotizacion.getCodi(), rowStyle);
                    createCell(row, colNum++, cotizacion.getCodf(), rowStyle);
                    createCell(row, colNum++, cotizacion.getMarca(), rowStyle);

                    Cell stockCell = row.createCell(colNum++);
                    stockCell.setCellValue(cotizacion.getStock() != null ? cotizacion.getStock().doubleValue() : 0);
                    stockCell.setCellStyle(numberStyle);

                    Cell stockFechaCell = row.createCell(colNum++);
                    stockFechaCell.setCellValue(cotizacion.getStockFechaCotizacion() != null
                            ? cotizacion.getStockFechaCotizacion().doubleValue()
                            : 0);
                    stockFechaCell.setCellStyle(numberStyle);

                    Cell cantCell = row.createCell(colNum++);
                    cantCell.setCellValue(cotizacion.getCantidadCotizacion() != null
                            ? cotizacion.getCantidadCotizacion().doubleValue()
                            : 0);
                    cantCell.setCellStyle(numberStyle);

                    createCell(row, colNum++, cotizacion.getMoneda(), rowStyle);

                    Cell precioCell = row.createCell(colNum++);
                    precioCell.setCellValue(
                            cotizacion.getPrecioDolares() != null ? cotizacion.getPrecioDolares().doubleValue() : 0);
                    precioCell.setCellStyle(currencyStyle);

                    createCell(row, colNum++, cotizacion.getCodCliente(), rowStyle);
                    createCell(row, colNum++, cotizacion.getRznCliente(), rowStyle);
                    createCell(row, colNum++, cotizacion.getNroCotizacion(), rowStyle);

                    Cell fechaCell = row.createCell(colNum++);
                    if (cotizacion.getFecha() != null) {
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.clear();
                        cal.set(cotizacion.getFecha().getYear(),
                                cotizacion.getFecha().getMonthValue() - 1,
                                cotizacion.getFecha().getDayOfMonth());

                        fechaCell.setCellValue(cal.getTime());
                        fechaCell.setCellStyle(dateStyle);

                        if (rowNum <= 5) {
                            logger.debug("Fila {}: Fecha original: {}, Fecha en Excel: {}",
                                    rowNum, cotizacion.getFecha(),
                                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                                            cal.getTime().toInstant().atZone(java.time.ZoneId.systemDefault())
                                                    .toLocalDate()));
                        }
                    } else {
                        fechaCell.setCellStyle(rowStyle);
                    }

                    createCell(row, colNum++, cotizacion.getVendedor(), rowStyle);
                    createCell(row, colNum++, cotizacion.getOrden(), rowStyle);
                    createCell(row, colNum++, cotizacion.getTipoOC(), rowStyle);
                    createCell(row, colNum++, cotizacion.getProveedor(), rowStyle);

                    Cell precioOCCell = row.createCell(colNum++);
                    precioOCCell.setCellValue(
                            cotizacion.getPreOCDolar() != null ? cotizacion.getPreOCDolar().doubleValue() : 0);
                    precioOCCell.setCellStyle(currencyStyle);

                    createCell(row, colNum++, cotizacion.getNotaIngreso(), rowStyle);
                    createCell(row, colNum++, cotizacion.getFechaIng(), rowStyle);

                    Cell cpuCell = row.createCell(colNum++);
                    cpuCell.setCellValue(cotizacion.getCpu() != null ? cotizacion.getCpu().doubleValue() : 0);
                    cpuCell.setCellStyle(numberStyle);
                }

                Row footerRow = sheet.createRow(rowNum + 1);
                Cell footerCell = footerRow.createCell(0);
                footerCell.setCellValue(
                        "Reporte generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                " - Total registros: " + cotizaciones.size());
                footerCell.setCellStyle(subHeaderStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum + 1, rowNum + 1, 0, 19));

                sheet.createFreezePane(0, 4);

                sheet.setDisplayGridlines(false);
                sheet.setZoom(100);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);

                String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String filename = "Cotizaciones_Pendientes_" + fecha + ".xlsx";

                return ResponseEntity.ok()
                        .contentType(MediaType
                                .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .body(outputStream.toByteArray());
            }

        } catch (Exception e) {
            logger.error("Error al generar Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private void createCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setTopBorderColor(IndexedColors.WHITE.getIndex());
        style.setRightBorderColor(IndexedColors.WHITE.getIndex());
        style.setBottomBorderColor(IndexedColors.WHITE.getIndex());
        style.setLeftBorderColor(IndexedColors.WHITE.getIndex());
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setWrapText(true);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createSubHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.0000"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTextStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createAlternateRowStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createHighlightStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
}