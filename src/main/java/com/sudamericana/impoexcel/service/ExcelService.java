package com.sudamericana.impoexcel.service;

import com.sudamericana.impoexcel.model.Product;
import com.sudamericana.impoexcel.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.Iterator;

@Service
public class ExcelService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);
    
    @Autowired
    private ProductRepository productRepository;
    @Autowired
private JdbcTemplate jdbcTemplate;


    @Transactional
public void saveAll(List<Product> products) {
    if (products != null && !products.isEmpty()) {
        logger.info("Intentando guardar {} productos en la base de datos", products.size());
        
        // Imprimir detalles de TODOS los producto
        logger.info("*** LISTA COMPLETA DE PRODUCTOS A GUARDAR ***");
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            logger.info("Producto {}: Código={}, Marca={}, Stock={}, Precio={}, Proveedor={}, País={}",
                    i+1, p.getCodigo(), p.getMarca(), p.getStock(), p.getPrecio(), p.getProveedor(), p.getPais());
        }
        
        try {
            // Guardar uno por uno 
            List<Product> savedProducts = new ArrayList<>();
            for (Product product : products) {
                try {
                    // Verificar ID nulo para confirmar que es un nuevo producto
                    if (product.getId() != null) {
                        logger.warn("Producto con ID ya existente: {}, se tratará como actualización", product.getId());
                    }
                    
                    // Guardar y recuperar el producto guardado
                    Product saved = productRepository.save(product);
                    savedProducts.add(saved);
                    logger.info("Producto guardado exitosamente: ID={}, Código={}", 
                            saved.getId(), saved.getCodigo());
                } catch (Exception e) {
                    logger.error("Error al guardar producto individual {}: {}", 
                            product.getCodigo(), e.getMessage(), e);
                }
            }
            
            logger.info("Se han guardado {} productos exitosamente de un total de {}", 
                    savedProducts.size(), products.size());
            
            productRepository.flush();
            
        } catch (Exception e) {
            logger.error("Error al guardar productos: {}", e.getMessage(), e);
            throw e;
        }
    } else {
        logger.warn("No se guardaron productos porque la lista estaba vacía");
    }
}

@Transactional
public void importarExcel(InputStream excelFile) throws Exception {
    logger.info("Iniciando importación de Excel");
    
    List<Product> products = new ArrayList<>();
    try (Workbook workbook = new XSSFWorkbook(excelFile)) {
        Sheet sheet = workbook.getSheetAt(0);
        int rowCount = 0;
        int successCount = 0;
        int errorCount = 0;
        
        Iterator<Row> rowIterator = sheet.iterator();
        
        if (rowIterator.hasNext()) {
            Row headerRow = rowIterator.next();
            
            int codigoIndex = -1;
            int marcaIndex = -1;
            int stockIndex = -1;
            int precioIndex = -1;
            int proveedorIndex = -1;
            int paisIndex = -1;
            
            for (int i = 0; i < 10; i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String header = getCellValueAsString(cell).trim().toUpperCase();
                    if (header.contains("CÓDIGO") || header.contains("CODIGO")) codigoIndex = i;
                    else if (header.contains("MARCA")) marcaIndex = i;
                    else if (header.contains("STOCK") || header.contains("CANTIDAD")) stockIndex = i;
                    else if (header.contains("PRECIO")) precioIndex = i;
                    else if (header.contains("PROVEEDOR")) proveedorIndex = i;
                    else if (header.contains("PAIS") || header.contains("PAÍS")) paisIndex = i;
                }
            }
            
            logger.info("Índices de columnas: Código={}, Marca={}, Stock={}, Precio={}, Proveedor={}, País={}", 
                    codigoIndex, marcaIndex, stockIndex, precioIndex, proveedorIndex, paisIndex);
            
            if (codigoIndex == -1 || marcaIndex == -1 || stockIndex == -1 || 
                precioIndex == -1 || proveedorIndex == -1) {
                throw new IllegalArgumentException("No se encontraron todas las columnas requeridas en el archivo");
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowCount++;
                
                try {
                    String codigo = getCellValueAsString(row.getCell(codigoIndex)).trim();
                    String marca = getCellValueAsString(row.getCell(marcaIndex)).trim();
                    
                    int stock = 0;
                    Cell stockCell = row.getCell(stockIndex);
                    if (stockCell != null) {
                        if (stockCell.getCellType() == CellType.STRING) {
                            try {
                                String stockStr = stockCell.getStringCellValue().trim();
                                stockStr = stockStr.replaceAll("[^0-9]", "");
                                if (!stockStr.isEmpty()) {
                                    stock = Integer.parseInt(stockStr);
                                }
                            } catch (Exception e) {
                                logger.warn("Error al convertir stock de texto en fila {}: {}", rowCount, e.getMessage());
                            }
                        } else {
                            try {
                                stock = (int) Math.round(stockCell.getNumericCellValue());
                            } catch (Exception e) {
                                logger.warn("Error al convertir stock numérico en fila {}: {}", rowCount, e.getMessage());
                            }
                        }
                    }
                    logger.info("Usando stock={} para fila {} con código {}", stock, rowCount, codigo);
                    
                    double precio = 0.0;
Cell precioCell = row.getCell(precioIndex);
if (precioCell != null) {
    try {
        switch (precioCell.getCellType()) {
            case NUMERIC:
                precio = precioCell.getNumericCellValue();
                break;
            case STRING:
                String precioStr = precioCell.getStringCellValue().trim();
                if (!precioStr.isEmpty()) {
                    precioStr = precioStr.replaceAll("[$€£¥]|\\s", "");
                    
                    if (precioStr.contains(",") && precioStr.contains(".")) {
                        if (precioStr.lastIndexOf(",") > precioStr.lastIndexOf(".")) {
                            precioStr = precioStr.replace(".", "").replace(",", ".");
                        } else {
                            precioStr = precioStr.replace(",", "");
                        }
                    } else {
                        precioStr = precioStr.replace(",", ".");
                    }
                    
                    try {
                        precio = Double.parseDouble(precioStr);
                    } catch (NumberFormatException e) {
                        logger.warn("No se pudo convertir precio '{}' a número en la fila {}: {}", 
                                precioCell.getStringCellValue(), rowCount, e.getMessage());
                    }
                }
                break;
            case FORMULA:
                try {
                    precio = precioCell.getNumericCellValue();
                } catch (Exception e) {
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(precioCell);
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        precio = cellValue.getNumberValue();
                    } else if (cellValue.getCellType() == CellType.STRING) {
                        String formulaResult = cellValue.getStringValue().trim()
                                .replaceAll("[$€£¥]|\\s", "")
                                .replace(",", ".");
                        try {
                            precio = Double.parseDouble(formulaResult);
                        } catch (NumberFormatException ex) {
                            logger.warn("No se pudo convertir el resultado de la fórmula '{}' a número en la fila {}", 
                                    cellValue.getStringValue(), rowCount);
                        }
                    }
                }
                break;
            default:
                precio = 0.0;
        }
    } catch (Exception e) {
        logger.warn("Error al procesar precio en la fila {}: {}", rowCount, e.getMessage());
    }
}

logger.info("Precio extraído para fila {} con código {}: {}", rowCount, codigo, precio);
                    
if (precio < 0) {
    logger.warn("Precio negativo ({}) en fila {}, se establecerá a 0", precio, rowCount);
    precio = 0.0;
} else if (precio == 0) {
    logger.info("Producto con precio 0 en fila {} con código {}", rowCount, codigo);
}

                    String proveedor = getCellValueAsString(row.getCell(proveedorIndex)).trim();
                    
                    String pais = paisIndex >= 0 ? getCellValueAsString(row.getCell(paisIndex)).trim() : "";
                    pais = (pais == null || pais.trim().isEmpty()) ? "N/A" : pais;
                    
                    // Validar datos básicos
                    if (codigo.isEmpty()) {
                        logger.warn("Fila {} ignorada: código vacío", rowCount);
                        errorCount++;
                        continue;
                    }
                    
                    if (marca.isEmpty()) {
                        logger.warn("Fila {} ignorada: marca vacía", rowCount);
                        errorCount++;
                        continue;
                    }
                    
                    if (precio <= 0) {
                        logger.warn("Fila {} ignorada: precio inválido ({})", rowCount, precio);
                        errorCount++;
                        continue;
                    }
                    
                    if (proveedor.isEmpty()) {
                        proveedor = "No especificado";
                        logger.info("Estableciendo proveedor predeterminado para fila {}", rowCount);
                    }
                    
                    // Crear y añadir el producto
                    Product product = new Product();
                    product.setCodigo(codigo);
                    product.setMarca(marca);
                    product.setStock(stock);
                    product.setPrecio(precio);
                    product.setProveedor(proveedor);
                    product.setPais(pais);
                    product.setFechaCreacion(new Date());
                    
                    products.add(product);
                    successCount++;
                    logger.info("Producto añadido: Código={}, Marca={}, Stock={}, Precio={}",
                            codigo, marca, stock, precio);
                    
                } catch (Exception e) {
                    logger.error("Error procesando fila {}: {}", rowCount, e.getMessage(), e);
                    errorCount++;
                }
            }
        } else {
            throw new IllegalArgumentException("El archivo Excel está vacío");
        }
        
        logger.info("Procesamiento completado: {} filas procesadas, {} productos válidos, {} errores", 
                rowCount, successCount, errorCount);
        
        if (!products.isEmpty()) {
            insertarDirectamente(products);
        } else {
            logger.warn("No hay productos para guardar después del procesamiento");
        }
    } catch (IOException e) {
        logger.error("Error al procesar el archivo Excel: {}", e.getMessage());
        throw new Exception("Error al procesar el archivo Excel: " + e.getMessage(), e);
    }
}

private void validateHeaders(Row headerRow) {
    Map<String, String> expectedHeaderMap = new HashMap<>();
    expectedHeaderMap.put("CODIGO", "Código");
    expectedHeaderMap.put("MARCA", "Marca");
    expectedHeaderMap.put("STOCK", "Stock");
    expectedHeaderMap.put("PRECIO", "Precio");
    expectedHeaderMap.put("PROVEEDOR", "PROVEEDOR");
    expectedHeaderMap.put("PAIS", "Pais");
    
    int cellCount = headerRow.getPhysicalNumberOfCells();
    for (int i = 0; i < cellCount; i++) {
        String actualHeader = getCellValueAsString(headerRow.getCell(i)).trim();
        if (!actualHeader.isEmpty()) {
            String normalizedHeader = actualHeader.toUpperCase()
                    .replace("Í", "I")
                    .replace("Ó", "O")
                    .replace("Á", "A")
                    .replace("É", "E")
                    .replace("Ú", "U");
            
            for (Map.Entry<String, String> entry : expectedHeaderMap.entrySet()) {
                if (normalizedHeader.contains(entry.getKey())) {
                    logger.info("Cabecera encontrada: '{}' corresponde a '{}'", 
                            actualHeader, entry.getValue());
                    break;
                }
            }
        }
    }
}

@Transactional
public void insertarDirectamente(List<Product> products) {
    if (products == null || products.isEmpty()) {
        logger.warn("No hay productos para insertar");
        return;
    }
    
    logger.info("Iniciando inserción directa de {} productos", products.size());
    
    int insertados = 0;
    int errores = 0;
    
    for (Product p : products) {
        try {
            logger.info("Intentando insertar: Código={}, Marca={}, Stock={}, Precio={}, Proveedor={}, País={}",
                p.getCodigo(), p.getMarca(), p.getStock(), p.getPrecio(), p.getProveedor(), p.getPais());
            
            int result = jdbcTemplate.update(
                "INSERT INTO productoimpo_sw (codigo, marca, stock, precio, proveedor, pais, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?)",
                p.getCodigo(),
                p.getMarca(),
                p.getStock(),
                p.getPrecio(),
                p.getProveedor(),
                p.getPais(),
                p.getFechaCreacion()
            );
            
            if (result > 0) {
                insertados++;
                logger.info("Producto insertado exitosamente: {}", p.getCodigo());
            } else {
                logger.warn("No se insertó el producto {}", p.getCodigo());
            }
        } catch (Exception e) {
            errores++;
            logger.error("Error al insertar producto {}: {}", p.getCodigo(), e.getMessage());
        }
    }
    
    logger.info("Resumen de inserción directa: {} insertados, {} errores de {} intentados", 
            insertados, errores, products.size());
}

@Transactional(readOnly = true)
public void verificarEstructuraTabla() {
    try {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT " +
            "FROM INFORMATION_SCHEMA.COLUMNS " +
            "WHERE TABLE_NAME = 'productoimpo_sw'"
        );
        
        logger.info("Estructura de tabla productoimpo_sw:");
        for (Map<String, Object> column : columns) {
            logger.info("Columna: {} - Tipo: {} - ¿Nullable?: {} - Default: {}", 
                column.get("COLUMN_NAME"), 
                column.get("DATA_TYPE"), 
                column.get("IS_NULLABLE"),
                column.get("COLUMN_DEFAULT"));
        }
    } catch (Exception e) {
        logger.error("Error al verificar estructura de tabla: {}", e.getMessage());
    }
}

public byte[] exportarExcelPorFechas(Date fechaInicio, Date fechaFin) throws IOException {
    fechaFin = ajustarFechaFin(fechaFin);
    
    // Formato para mostrar las fechas en el nombre de la hoja
    SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat sdfSheetName = new SimpleDateFormat("dd-MM-yyyy");
    
    String periodoDisplay = sdfDisplay.format(fechaInicio) + " - " + sdfDisplay.format(fechaFin);
    String periodoSheetName = sdfSheetName.format(fechaInicio) + " a " + sdfSheetName.format(fechaFin);

    List<Product> productos = productRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);

    if (productos.isEmpty()) {
        logger.info("No se encontraron productos en el rango de fechas especificado.");
        return new byte[0];
    }

    Map<String, List<Product>> productosAgrupados = productos.stream()
            .collect(Collectors.groupingBy(Product::getCodigo));

    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        // Crear hoja con nombre que incluye el período
        Sheet sheet = workbook.createSheet(validarNombreHoja("Productos " + periodoSheetName));
        
        // Crear estilos para el reporte
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataCellStyle = createDataCellStyle(workbook);
        CellStyle numberCellStyle = createNumberCellStyle(workbook);
        CellStyle currencyCellStyle = createCurrencyStyle(workbook);
        CellStyle alternateRowStyle = createAlternateRowStyle(workbook);
        
        // Crear título del reporte
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE PRODUCTOS");
        titleCell.setCellStyle(titleStyle);
        
        // Fusionar celdas título
        int maxProveedores = productosAgrupados.values().stream()
                .mapToInt(List::size)
                .max()
                .orElse(1);
        int lastColumn = Math.max(5, (maxProveedores * 5));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastColumn));
        
        // Creasubtítulo
        Row subtitleRow = sheet.createRow(1);
        subtitleRow.setHeightInPoints(20);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("Período: " + periodoDisplay);
        subtitleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, lastColumn));
        
        // Crear fila de encabezados
        Row headerRow = sheet.createRow(3);
        headerRow.setHeightInPoints(18);
        
        Cell codigoCell = headerRow.createCell(0);
        codigoCell.setCellValue("Código");
        codigoCell.setCellStyle(headerStyle);

        for (int i = 0; i < maxProveedores; i++) {
            int baseIndex = 1 + (i * 5);
            
            Cell marcaCell = headerRow.createCell(baseIndex);
            marcaCell.setCellValue("Marca " + (i + 1));
            marcaCell.setCellStyle(headerStyle);

            Cell stockCell = headerRow.createCell(baseIndex + 1);
            stockCell.setCellValue("Stock " + (i + 1));
            stockCell.setCellStyle(headerStyle);

            Cell precioCell = headerRow.createCell(baseIndex + 2);
            precioCell.setCellValue("Precio " + (i + 1));
            precioCell.setCellStyle(headerStyle);

            Cell proveedorCell = headerRow.createCell(baseIndex + 3);
            proveedorCell.setCellValue("Proveedor " + (i + 1));
            proveedorCell.setCellStyle(headerStyle);

            Cell paisCell = headerRow.createCell(baseIndex + 4);
            paisCell.setCellValue("País " + (i + 1));
            paisCell.setCellStyle(headerStyle);
        }

        // Ajustar ancho de columnas
        sheet.setColumnWidth(0, 6000);
        for (int i = 1; i <= lastColumn; i++) {
            sheet.setColumnWidth(i, 4000);
        }

        // Agregar datos
        int rowIndex = 4;
        for (Map.Entry<String, List<Product>> entry : productosAgrupados.entrySet()) {
            String codigo = entry.getKey();
            List<Product> listaProductos = entry.getValue();

            Row row = sheet.createRow(rowIndex);
            
            CellStyle rowStyle = (rowIndex % 2 == 0) ? dataCellStyle : alternateRowStyle;
            
            Cell codigoDataCell = row.createCell(0);
            codigoDataCell.setCellValue(codigo);
            codigoDataCell.setCellStyle(rowStyle);

            int colIndex = 1;
            for (Product p : listaProductos) {
                // Marca
                Cell marcaDataCell = row.createCell(colIndex++);
                marcaDataCell.setCellValue(p.getMarca());
                marcaDataCell.setCellStyle(rowStyle);
                
                // Stock
                Cell stockDataCell = row.createCell(colIndex++);
                stockDataCell.setCellValue(p.getStock());
                stockDataCell.setCellStyle(numberCellStyle);
                
                // Precio
                Cell precioDataCell = row.createCell(colIndex++);
                precioDataCell.setCellValue(p.getPrecio());
                precioDataCell.setCellStyle(currencyCellStyle);
                
                // Proveedor
                Cell proveedorDataCell = row.createCell(colIndex++);
                proveedorDataCell.setCellValue(p.getProveedor());
                proveedorDataCell.setCellStyle(rowStyle);
                
                // País
                Cell paisDataCell = row.createCell(colIndex++);
                paisDataCell.setCellValue(p.getPais() != null && !p.getPais().trim().isEmpty() ? p.getPais() : "N/A");
                paisDataCell.setCellStyle(rowStyle);
            }
            
            rowIndex++;
        }
        
        rowIndex += 2;
        Row summaryRow = sheet.createRow(rowIndex++);
        Cell summaryLabelCell = summaryRow.createCell(0);
        summaryLabelCell.setCellValue("Total de productos:");
        summaryLabelCell.setCellStyle(headerStyle);
        
        Cell summaryValueCell = summaryRow.createCell(1);
        summaryValueCell.setCellValue(productos.size());
        summaryValueCell.setCellStyle(numberCellStyle);
        
        Row dateRow = sheet.createRow(rowIndex++);
        Cell dateLabelCell = dateRow.createCell(0);
        dateLabelCell.setCellValue("Reporte generado el:");
        dateLabelCell.setCellStyle(headerStyle);
        
        Cell dateValueCell = dateRow.createCell(1);
        dateValueCell.setCellValue(new Date());
        
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        dateStyle.setAlignment(HorizontalAlignment.LEFT);
        dateStyle.setBorderBottom(BorderStyle.THIN);
        dateStyle.setBorderLeft(BorderStyle.THIN);
        dateStyle.setBorderRight(BorderStyle.THIN);
        dateStyle.setBorderTop(BorderStyle.THIN);
        dateValueCell.setCellStyle(dateStyle);

        sheet.createFreezePane(1, 4);
        
        sheet.protectSheet("password");
        
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}

private CellStyle createTitleStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font titleFont = workbook.createFont();
    titleFont.setFontHeightInPoints((short) 16);
    titleFont.setBold(true);
    titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFont(titleFont);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.MEDIUM);
    style.setBorderTop(BorderStyle.MEDIUM);
    return style;
}


private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setFontHeightInPoints((short) 11);
    headerFont.setBold(true);
    headerFont.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(headerFont);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setWrapText(true);
    return style;
}


private CellStyle createDataCellStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.LEFT);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    return style;
}


private CellStyle createNumberCellStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.cloneStyleFrom(createDataCellStyle(workbook));
    style.setAlignment(HorizontalAlignment.RIGHT);
    style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
    return style;
}


private CellStyle createCurrencyStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.cloneStyleFrom(createDataCellStyle(workbook));
    style.setAlignment(HorizontalAlignment.RIGHT);
    style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
    return style;
}


private CellStyle createAlternateRowStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.cloneStyleFrom(createDataCellStyle(workbook));
    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return style;
}

    private Date ajustarFechaFin(Date fechaFin) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaFin);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.format("%d", (long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellValue cellValue = evaluator.evaluate(cell);
                switch (cellValue.getCellType()) {
                    case STRING:
                        return cellValue.getStringValue();
                    case NUMERIC:
                        double formulaNumericValue = cellValue.getNumberValue();
                        if (formulaNumericValue == (long) formulaNumericValue) {
                            return String.format("%d", (long) formulaNumericValue);
                        } else {
                            return String.valueOf(formulaNumericValue);
                        }
                    case BOOLEAN:
                        return String.valueOf(cellValue.getBooleanValue());
                    default:
                        return "";
                }
            default:
                return "";
        }
    }

    /**
 * Valida y corrige un nombre de hoja de Excel para asegurar que sea válido
 * @param nombreOriginal Nombre original propuesto para la hoja
 * @return Nombre de hoja válido para Excel
 */
private String validarNombreHoja(String nombreOriginal) {
    if (nombreOriginal == null || nombreOriginal.isEmpty()) {
        return "Hoja1";
    }
    
    String nombreValido = nombreOriginal
            .replace('/', '-')
            .replace('\\', '-')
            .replace('?', '_')
            .replace('*', '_')
            .replace(':', '-')
            .replace('[', '(')
            .replace(']', ')')
            .replace('\'', ' ');
    
    if (nombreValido.length() > 31) {
        nombreValido = nombreValido.substring(0, 31);
    }
    
    return nombreValido;
}

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        switch (cell.getCellType()) {
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            case NUMERIC:
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue() ? 1.0 : 0.0;
            case FORMULA:
                return cell.getNumericCellValue();
            default:
                return 0.0;
        }
    }

    /**
 * @param termino Término de búsqueda (código, marca o proveedor)
 * @param pageable Paginación
 * @return Página de productos que coincidan con la búsqueda
 */
public Page<Product> buscarProductosFuzzy(String termino, Pageable pageable) {
    if (termino == null || termino.trim().isEmpty()) {
        logger.info("Buscando todos los productos");
        return productRepository.findAll(pageable);
    }

    String terminoNormalizado = normalizarTexto(termino);
    String terminoSinEspacios = terminoNormalizado.replaceAll("\\s", "");
    
    logger.info("Buscando productos con término: '{}' (normalizado: '{}')", termino, terminoSinEspacios);

    List<String> codigosCoincidentes = new ArrayList<>();
    
    List<Product> productos = productRepository.findAll();
    for (Product producto : productos) {
        String codigoNormalizado = normalizarTexto(producto.getCodigo());
        String codigoCompacto = codigoNormalizado.replaceAll("\\s", "");
        
        if (codigoCompacto.contains(terminoSinEspacios)) {
            codigosCoincidentes.add(producto.getCodigo());
        }
    }
    
    logger.info("Se encontraron {} códigos coincidentes para el término '{}'", 
            codigosCoincidentes.size(), termino);

    return productRepository.findByCodigoInOrMarcaContainingOrProveedorContaining(
            codigosCoincidentes,
            termino,
            termino,
            pageable);
}

    /**
     * @param termino Término para buscar códigos
     * @return Lista de códigos sugeridos (máximo 10)
     */
 public List<String> obtenerSugerenciasCodigoFuzzy(String termino) {
    if (termino == null || termino.trim().length() < 2) {
        return Collections.emptyList();
    }

    String terminoNormalizado = normalizarTexto(termino);
    String terminoSinEspacios = terminoNormalizado.replaceAll("\\s", "");
    
    logger.info("Buscando sugerencias para: '{}' (normalizado: '{}')", termino, terminoSinEspacios);

    Set<String> codigosCoincidentes = new HashSet<>();
    
    List<Product> productos = productRepository.findAll();
    for (Product producto : productos) {
        String codigoNormalizado = normalizarTexto(producto.getCodigo());
        String codigoCompacto = codigoNormalizado.replaceAll("\\s", "");
        
        if (codigoCompacto.contains(terminoSinEspacios)) {
            codigosCoincidentes.add(producto.getCodigo());
        }
    }
    
    productRepository.findByCodigoContainingIgnoreCase(terminoSinEspacios)
            .forEach(p -> codigosCoincidentes.add(p.getCodigo()));
    
    logger.info("Se encontraron {} sugerencias para el término '{}'", 
            codigosCoincidentes.size(), termino);
    
    return codigosCoincidentes.stream()
            .limit(10)
            .sorted()
            .collect(Collectors.toList());
}
/**
 * Normaliza el texto para búsquedas más eficientes
 * Este método mejorado preserva la relación entre caracteres originales
 * @param texto Texto a normalizar
 * @return Texto normalizado
 */
private String normalizarTexto(String texto) {
    if (texto == null) return "";
    
    String resultado = texto.toLowerCase();
    
    resultado = resultado
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
            .replace('ü', 'u')
            .replace('ñ', 'n');
    
    resultado = resultado.replaceAll("[\\-_.,;:()\\[\\]{}]", "");
    
    resultado = resultado.replaceAll("\\s+", " ");
    
    String sinEspacios = resultado.replaceAll("\\s", "");
    
    return sinEspacios;
}

    public Page<Product> buscarPorCodigo(String codigo, Pageable pageable) {
        return productRepository.findByCodigo(codigo, pageable);
    }

    public Page<Product> listarTodos(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
}