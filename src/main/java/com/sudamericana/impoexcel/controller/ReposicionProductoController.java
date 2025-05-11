package com.sudamericana.impoexcel.controller;

import com.sudamericana.impoexcel.model.ReposicionFiltro;
import com.sudamericana.impoexcel.model.ReposicionProducto;
import com.sudamericana.impoexcel.service.AlmacenService;
import com.sudamericana.impoexcel.service.MarcaService;
import com.sudamericana.impoexcel.service.ReposicionProductoService;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/reposicion-productos")
public class ReposicionProductoController {

    private final ReposicionProductoService reposicionService;
    private final AlmacenService almacenService;
    private final MarcaService marcaService;
    private static final Logger logger = LoggerFactory.getLogger(ReposicionProductoController.class);

    @Autowired
    public ReposicionProductoController(
            ReposicionProductoService reposicionService,
            AlmacenService almacenService,
            MarcaService marcaService) {
        this.reposicionService = reposicionService;
        this.almacenService = almacenService;
        this.marcaService = marcaService;
    }

    @GetMapping
    public String mostrarFormulario(Model model) {
        try {
            ReposicionFiltro filtro = new ReposicionFiltro();
            model.addAttribute("filtro", filtro);

            model.addAttribute("marcas", marcaService.listarTodas());
            model.addAttribute("almacenes", almacenService.listarAlmacenes());

            logger.info("Formulario de reposición cargado correctamente");
            return "reposicion/formulario";
        } catch (Exception e) {
            logger.error("Error al mostrar el formulario de reposición de productos", e);
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "error";
        }
    }

    private CellStyle createOptimoStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBajoStockStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createSobreStockStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    @PostMapping("/exportar")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute("filtro") ReposicionFiltro filtro) {
        try {
            logger.info("Exportando reporte de reposición con filtro: {}", filtro);

            List<ReposicionProducto> productos = reposicionService.buscarReposicionProductos(filtro);
            logger.info("Se encontraron {} productos para el reporte", productos.size());

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Reposición de Productos");

                CellStyle titleStyle = createTitleStyle(workbook);
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
                CellStyle dateStyle = createDateStyle(workbook);
                CellStyle numberStyle = createNumberStyle(workbook);
                CellStyle currencyStyle = createCurrencyStyle(workbook);
                CellStyle textStyle = createTextStyle(workbook);
                CellStyle alternateStyle = createAlternateRowStyle(workbook);
                CellStyle highlightStyle = createHighlightStyle(workbook);
                CellStyle optimoStyle = createOptimoStyle(workbook);
                CellStyle bajoStockStyle = createBajoStockStyle(workbook);
                CellStyle sobreStockStyle = createSobreStockStyle(workbook);

                Row titleRow = sheet.createRow(0);
                titleRow.setHeight((short) 800);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("REPORTE DE REPOSICIÓN DE PRODUCTOS");
                titleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 30));

                Row paramRow = sheet.createRow(1);
                paramRow.setHeight((short) 500);

                createCell(paramRow, 0, "Almacén:", subHeaderStyle);
                createCell(paramRow, 1, filtro.getCodAlm().equals("00") ? "TODOS"
                        : almacenService.obtenerNombreAlmacen(filtro.getCodAlm()), textStyle);

                createCell(paramRow, 3, "Marca:", subHeaderStyle);
                createCell(paramRow, 4, filtro.getMarca().equals("0000") ? "TODAS" : filtro.getMarca(), textStyle);

                createCell(paramRow, 6, "Meses Venta:", subHeaderStyle);
                createCell(paramRow, 7, filtro.getMesesVenta().toString(), textStyle);

                createCell(paramRow, 9, "Días Demora Proveedor:", subHeaderStyle);
                createCell(paramRow, 10, filtro.getDiasDemoraProveedor().toString(), textStyle);

                createCell(paramRow, 12, "Factor Días Seguridad:", subHeaderStyle);
                createCell(paramRow, 13, filtro.getFactorDiasSeguridad().toString(), textStyle);

                createCell(paramRow, 15, "Estado:", subHeaderStyle);
                createCell(paramRow, 16, String.join(", ", filtro.getEstados()), textStyle);

                Row spacerRow = sheet.createRow(2);
                spacerRow.setHeight((short) 300);

                Row headerRow = sheet.createRow(3);
                headerRow.setHeight((short) 600);

                String[] baseHeaders = {
                        "Clase", "Codi", "Codf", "Descripción", "Marca", "Cantidad\nVendida",
                        "Meses\nVentas", "Nro de Ventas", "%Mes", "%Día", "Stock\nTotal",
                        "Factor\nSeguridad", "Reponer", "Estado", "Fecha\nÚltima\nVenta", "Último\nPrecio\nCompra",
                        "Última Orden\nCompra", "Fecha\nIngreso\nAlmacén", "Proveedor", "Glosa"
                };

                List<String> headersList = new ArrayList<>(Arrays.asList(baseHeaders));

                if ("01".equals(filtro.getCodAlm())) {
                    headersList.add(headersList.indexOf("Cantidad\nVendida") + 1, "Cantidad\nVenta OS");
                    headersList.add(headersList.indexOf("Stock\nTotal") + 1, "Stock\nInicial");
                }

                String[] stockHeaders;
                if ("01".equals(filtro.getCodAlm())) {
                    stockHeaders = new String[] { "Lince", "San Luis", "Otros Almacenes", "Tránsito" };
                } else if ("03".equals(filtro.getCodAlm()) || "04".equals(filtro.getCodAlm())
                        || "02".equals(filtro.getCodAlm()) || "05".equals(filtro.getCodAlm())
                        || "06".equals(filtro.getCodAlm()) || "07".equals(filtro.getCodAlm())
                        || "08".equals(filtro.getCodAlm()) || "09".equals(filtro.getCodAlm())
                        || "10".equals(filtro.getCodAlm()) || "11".equals(filtro.getCodAlm())
                        || "12".equals(filtro.getCodAlm()) || "13".equals(filtro.getCodAlm())) {
                    stockHeaders = new String[] { "Lince", "San Luis", "Tránsito" };
                } else {
                    stockHeaders = new String[] {
                            "Lince", "Arequipa", "San Luis", "Trujillo", "Cerro\nColorado",
                            "Los Olivos", "Chiclayo", "Mercadería Obsoleta\nSanLuis", "Tomocorp 1",
                            "Obsoleta\nBaja\nMercadería", "Mercadería Obsoleta\nArequipa", "Tránsito"
                    };
                }

                headersList.addAll(Arrays.asList(stockHeaders));
                String[] headers = headersList.toArray(new String[0]);

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);

                    if ("01".equals(filtro.getCodAlm())) {
                        // Anchos personalizados para almacén 01
                        if (i == 1 || i == 2) { // Codi y Codf
                            sheet.setColumnWidth(i, 30 * 256);
                        } else if (i == 3) { // Descripción
                            sheet.setColumnWidth(i, 70 * 256);
                        } else if (i == 20) { // Proveedor
                            sheet.setColumnWidth(i, 60 * 256);
                        } else if (i == 21) { // Glosa
                            sheet.setColumnWidth(i, 30 * 256);
                        } else if (i == 18) { // Última Orden Compra
                            sheet.setColumnWidth(i, 20 * 256);
                        } else if (i >= 22) { // Columnas de stock (Lince, San Luis, Otros Almacenes, Tránsito)
                            sheet.setColumnWidth(i, 18 * 256);
                        } else if (i == 4) { // Marca
                            sheet.setColumnWidth(i, 15 * 256);
                        } else if (i == 6) { // Cantidad Venta OS
                            sheet.setColumnWidth(i, 15 * 256);
                        } else if (i == 12) { // Stock Inicial
                            sheet.setColumnWidth(i, 15 * 256);
                        } else {
                            sheet.setColumnWidth(i, 12 * 256);
                        }
                    } else if ("03".equals(filtro.getCodAlm()) || "04".equals(filtro.getCodAlm())
                            || "02".equals(filtro.getCodAlm()) || "05".equals(filtro.getCodAlm())
                            || "06".equals(filtro.getCodAlm()) || "07".equals(filtro.getCodAlm())
                            || "08".equals(filtro.getCodAlm()) || "09".equals(filtro.getCodAlm())
                            || "10".equals(filtro.getCodAlm()) || "11".equals(filtro.getCodAlm())
                            || "12".equals(filtro.getCodAlm()) || "13".equals(filtro.getCodAlm())) {
                        // Anchos para otros almacenes específicos
                        if (i == 1 || i == 2) { // Codi y Codf
                            sheet.setColumnWidth(i, 30 * 256);
                        } else if (i == 3) { // Descripción
                            sheet.setColumnWidth(i, 70 * 256);
                        } else if (i == 20) { // Proveedor
                            sheet.setColumnWidth(i, 50 * 256);
                        } else if (i == 21) { // Glosa
                            sheet.setColumnWidth(i, 30 * 256);
                        } else if (i >= 22) { // Columnas de stock (Lince, San Luis, Tránsito)
                            sheet.setColumnWidth(i, 15 * 256);
                        } else {
                            sheet.setColumnWidth(i, 12 * 256);
                        }
                    } else {
                        // Anchos optimizados para el caso por defecto (todos los almacenes)
                        if (i == 2 || i == 3) { // 3 y 4
                            sheet.setColumnWidth(i, 45 * 256);
                        } else if (i == 3) { // 4
                            sheet.setColumnWidth(i, 25 * 256);
                        } else if (i == 1) { // 2
                            sheet.setColumnWidth(i, 15 * 256);
                        } else if (i == 14) { // 15
                            sheet.setColumnWidth(i, 15 * 256);
                        } else if (i == 16) { // Proveedor
                            sheet.setColumnWidth(i, 15 * 256);
                        } else if (i == 17) { // Glosa
                            sheet.setColumnWidth(i, 15 * 256);
                        } else if (i == 18) { // Proveedor
                            sheet.setColumnWidth(i, 40 * 256);
                        } else if (i == 19) { // Glosa
                            sheet.setColumnWidth(i, 25 * 256);
                        } else if (i >= 22) { // Columnas de stock
                            if (headers[i].contains("\n")) {
                                sheet.setColumnWidth(i, 10 * 256);
                            } else {
                                sheet.setColumnWidth(i, 10 * 256);
                            }
                        } else if (i == 4) { // Marca
                            sheet.setColumnWidth(i, 12 * 256);
                        } else if (i >= 5 && i <= 17) { // Columnas numéricas y datos
                            sheet.setColumnWidth(i, 10 * 256);
                        } else {
                            sheet.setColumnWidth(i, 12 * 256);
                        }
                    }
                }
                int rowNum = 4;
                boolean alternarColor = false;

                for (ReposicionProducto producto : productos) {
                    Row row = sheet.createRow(rowNum++);
                    CellStyle rowStyle = alternarColor ? alternateStyle : textStyle;
                    alternarColor = !alternarColor;

                    int colNum = 0;

                    createCell(row, colNum++, producto.getClase(), rowStyle);
                    createCell(row, colNum++, producto.getCodi(), rowStyle);
                    createCell(row, colNum++, producto.getCodf(), rowStyle);
                    createCell(row, colNum++, producto.getDescripcion(), rowStyle);
                    createCell(row, colNum++, producto.getMarca(), rowStyle);

                    Cell cantVendidaCell = row.createCell(colNum++);
                    cantVendidaCell.setCellValue(
                            producto.getCantidadVendida() != null ? producto.getCantidadVendida().doubleValue() : 0);
                    cantVendidaCell.setCellStyle(numberStyle);

                    if ("01".equals(filtro.getCodAlm())) {
                        Cell cantVendidaOtrasCell = row.createCell(colNum++);
                        cantVendidaOtrasCell.setCellValue(
                                producto.getCantVentOS() != null ? producto.getCantVentOS().doubleValue() : 0);
                        cantVendidaOtrasCell.setCellStyle(numberStyle);
                    }
                    Cell mesesVentasCell = row.createCell(colNum++);
                    mesesVentasCell.setCellValue(producto.getMesesVentas() != null ? producto.getMesesVentas() : 0);
                    mesesVentasCell.setCellStyle(numberStyle);

                    Cell nroVentasCell = row.createCell(colNum++);
                    nroVentasCell.setCellValue(producto.getNroVentas() != null ? producto.getNroVentas() : 0);
                    nroVentasCell.setCellStyle(numberStyle);

                    Cell porcMesCell = row.createCell(colNum++);
                    porcMesCell.setCellValue(
                            producto.getPorcentajeMes() != null ? producto.getPorcentajeMes().doubleValue() : 0);
                    porcMesCell.setCellStyle(numberStyle);

                    Cell porcDiaCell = row.createCell(colNum++);
                    porcDiaCell.setCellValue(
                            producto.getPorcentajeDia() != null ? producto.getPorcentajeDia().doubleValue() : 0);
                    porcDiaCell.setCellStyle(numberStyle);

                    Cell stockCell = row.createCell(colNum++);
                    stockCell.setCellValue(
                            producto.getStockTotal() != null ? producto.getStockTotal().doubleValue() : 0);
                    stockCell.setCellStyle(numberStyle);

                    if ("01".equals(filtro.getCodAlm())) {
                        Cell stockIniCell = row.createCell(colNum++);
                        stockIniCell.setCellValue(
                                producto.getStockIni() != null ? producto.getStockIni().doubleValue() : 0);
                        stockIniCell.setCellStyle(numberStyle);
                    }

                    Cell factorCell = row.createCell(colNum++);
                    factorCell.setCellValue(
                            producto.getFactorSeguridad() != null ? producto.getFactorSeguridad().doubleValue() : 0);
                    factorCell.setCellStyle(numberStyle);

                    Cell reponerCell = row.createCell(colNum++);
                    reponerCell.setCellValue(
                            producto.getCantidadReponer() != null ? producto.getCantidadReponer().doubleValue() : 0);
                    reponerCell.setCellStyle(numberStyle);

                    Cell estadoCell = row.createCell(colNum++);
                    estadoCell.setCellValue(producto.getEstado());

                    switch (producto.getEstado()) {
                        case "REPONER":
                            estadoCell.setCellStyle(highlightStyle);
                            break;
                        case "OPTIMO":
                            estadoCell.setCellStyle(optimoStyle);
                            break;
                        case "BAJO STOCK":
                            estadoCell.setCellStyle(bajoStockStyle);
                            break;
                        case "SOBRE STOCK":
                            estadoCell.setCellStyle(sobreStockStyle);
                            break;
                        default:
                            estadoCell.setCellStyle(rowStyle);
                    }

                    createCell(row, colNum++, producto.getFechaUltimaVenta(), rowStyle);

                    Cell precioCell = row.createCell(colNum++);
                    precioCell.setCellValue(
                            producto.getUltimoPrecioCompra() != null ? producto.getUltimoPrecioCompra().doubleValue()
                                    : 0);
                    CellStyle preciseCurrencyStyle = workbook.createCellStyle();
                    preciseCurrencyStyle.cloneStyleFrom(currencyStyle);
                    preciseCurrencyStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0000"));
                    precioCell.setCellStyle(preciseCurrencyStyle);

                    createCell(row, colNum++, producto.getUltimaOrdenCompra(), rowStyle);
                    createCell(row, colNum++, producto.getFechaIngresoAlmacen(), rowStyle);
                    createCell(row, colNum++, producto.getProveedor(), rowStyle);
                    createCell(row, colNum++, producto.getGlosa(), rowStyle);
                    if ("01".equals(filtro.getCodAlm())) {
                        addStockCell(row, colNum++, producto.getStockLince(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockSanLuis(), numberStyle);
                        addStockCell(row, colNum++, producto.getOtroAlm(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockTransito(), numberStyle);
                    } else if ("03".equals(filtro.getCodAlm()) || "04".equals(filtro.getCodAlm())
                            || "02".equals(filtro.getCodAlm()) || "05".equals(filtro.getCodAlm())
                            || "06".equals(filtro.getCodAlm()) || "07".equals(filtro.getCodAlm())
                            || "08".equals(filtro.getCodAlm()) || "09".equals(filtro.getCodAlm())
                            || "10".equals(filtro.getCodAlm()) || "11".equals(filtro.getCodAlm())
                            || "12".equals(filtro.getCodAlm()) || "13".equals(filtro.getCodAlm())) {
                        addStockCell(row, colNum++, producto.getStockLince(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockSanLuis(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockTransito(), numberStyle);
                    } else {
                        addStockCell(row, colNum++, producto.getStockLince(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockArequipa(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockSanLuis(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockTrujillo(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockCerroColorado(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockLosOlivos(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockChiclayo(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockMercaderiaObsoletaSanLuis(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockTomocorp1(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockObsoletaBajaMercaderia(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockMercaderiaObsoletaArequipa(), numberStyle);
                        addStockCell(row, colNum++, producto.getStockTransito(), numberStyle);
                    }
                }
                Row footerRow = sheet.createRow(rowNum + 1);
                Cell footerCell = footerRow.createCell(0);
                footerCell.setCellValue(
                        "Reporte generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                + " - Total registros: " + productos.size());
                footerCell.setCellStyle(subHeaderStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum + 1, rowNum + 1, 0, 30));
                sheet.createFreezePane(0, 4);
                sheet.setZoom(90);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);

                String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String filename = "Reposicion_Productos_" + fecha + ".xlsx";

                return ResponseEntity.ok()
                        .contentType(MediaType
                                .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .body(outputStream.toByteArray());
            }
        } catch (Exception e) {
            logger.error("Error al generar Excel de reposición: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private void addStockCell(Row row, int colIndex, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue(0);
        }
        cell.setCellStyle(style);
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
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
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