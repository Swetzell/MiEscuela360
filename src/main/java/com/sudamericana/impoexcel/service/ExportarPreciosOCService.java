package com.sudamericana.impoexcel.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold.RangeType;
import org.apache.poi.ss.usermodel.IconMultiStateFormatting.IconSet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;

@Service
public class ExportarPreciosOCService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void exportarAExcel(String numeroOC, OutputStream outputStream) throws Exception {
        try (InputStream plantillaStream = getClass()
                .getResourceAsStream("/templates/plantillas/Lista_De_PreciosV3.xlsm");
                Workbook workbook = WorkbookFactory.create(Objects.requireNonNull(plantillaStream))) {

            Sheet sheet = workbook.getSheetAt(0);
            CellStyle decimalStyle = createDecimalStyle(workbook);

            // filas pre-formateadas
            int filasPreformateadas = 152;

            // Initialize SheetConditionalFormatting
            SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

            // Guardar una fila de ejemplo para copiar estilos
            final Row filaEjemplo;
            if (sheet.getLastRowNum() >= 2) {
                filaEjemplo = sheet.getRow(2);
            } else if (sheet.getLastRowNum() >= 1) {
                filaEjemplo = sheet.getRow(1);
            } else {
                filaEjemplo = null;
            }

            final int[] filaActual = { 1 };
            final int[] totalFilasConDatos = { 0 };

            jdbcTemplate.query(
                    "EXEC obtener_datos_hc_sg @ndocu_param = ?",
                    new Object[] { numeroOC },
                    (ResultSet rs, int rowNum) -> {
                        Row row;

                        // Verificar si la fila ya existe
                        if (sheet.getRow(filaActual[0]) != null) {
                            row = sheet.getRow(filaActual[0]);
                        } else {

                            row = sheet.createRow(filaActual[0]);

                            if (filaEjemplo != null) {
                                row.setHeight(filaEjemplo.getHeight());
                            }

                            // Pre-crear celdas con formato para filas que exceden el límite
                            if (filaActual[0] > filasPreformateadas && filaEjemplo != null) {
                                // Copiar el formato de todas las celdas de la fila
                                for (int i = 0; i <= 28; i++) { // Ajustar por columnas
                                    Cell exampleCell = filaEjemplo.getCell(i);
                                    if (exampleCell != null) {
                                        Cell newCell = row.createCell(i);
                                        newCell.setCellStyle(exampleCell.getCellStyle());
                                    }
                                }
                            }
                        }

                        fillDataRowWithStyle(rs, row, decimalStyle, filaEjemplo, workbook);
                        filaActual[0]++;
                        totalFilasConDatos[0]++;
                        return null;
                    });

            // Limpiar filas sin datos pero mantener el formato de la plantilla
            for (int i = filaActual[0]; i <= filasPreformateadas; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j <= 28; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            CellStyle originalStyle = cell.getCellStyle();
                            switch (cell.getCellType()) {
                                case STRING:
                                    cell.setCellValue("");
                                    break;
                                case NUMERIC:
                                    cell.setCellValue(0);
                                    break;
                                case FORMULA:
                                    cell.setCellFormula(null);
                                    break;
                                case BOOLEAN:
                                    cell.setCellValue(false);
                                    break;
                                default:
                                    cell.setBlank();
                            }
                            cell.setCellStyle(originalStyle);
                        }
                    }
                }
            }

            // Ocultar filas después de los datos (opcional)
            for (int i = filaActual[0]; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    row.setZeroHeight(true); // Oculta la fila en lugar de eliminarla
                }
            }

            // Ajustar el área de impresión hasta donde hay datos
            workbook.setPrintArea(0, 0, 28, 1, filaActual[0] - 1);

            if (totalFilasConDatos[0] > 0) {
                IndexedColors goldColor = IndexedColors.GOLD;

                // Regla para fechas menores a 2 años
                ConditionalFormattingRule fechaRule = sheetCF.createConditionalFormattingRule(
                        "AND(M2<>\"\",YEAR(M2)<=2023)"); // fórmula para comparar el año directamente
                PatternFormatting fechaPattern = fechaRule.createPatternFormatting();
                fechaPattern.setFillBackgroundColor(goldColor.getIndex());
                fechaPattern.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

                CellRangeAddress[] fechaRanges = { CellRangeAddress.valueOf("M2:M" + (totalFilasConDatos[0] + 1)) };
                sheetCF.addConditionalFormatting(fechaRanges, fechaRule);

                // Regla para TipoOC
                ConditionalFormattingRule tipoOCRule = sheetCF.createConditionalFormattingRule(
                        "L2=\"COMPRA EN EL PAIS\"");
                PatternFormatting tipoOCPattern = tipoOCRule.createPatternFormatting();
                tipoOCPattern.setFillBackgroundColor(goldColor.getIndex());
                tipoOCPattern.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

                CellRangeAddress[] tipoOCRanges = { CellRangeAddress.valueOf("L2:L" + (totalFilasConDatos[0] + 1)) };
                sheetCF.addConditionalFormatting(tipoOCRanges, tipoOCRule);

                // Regla para nompro
                ConditionalFormattingRule nomproRule = sheetCF.createConditionalFormattingRule(
                        "L2=\"COMPRA EN EL PAIS\"");
                PatternFormatting nomproPattern = nomproRule.createPatternFormatting();
                nomproPattern.setFillBackgroundColor(goldColor.getIndex());
                nomproPattern.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

                CellRangeAddress[] nomproRanges = { CellRangeAddress.valueOf("N2:N" + (totalFilasConDatos[0] + 1)) };
                sheetCF.addConditionalFormatting(nomproRanges, nomproRule);
            }

            workbook.write(outputStream);
        }
    }

    private void fillDataRowWithStyle(ResultSet rs, Row row, CellStyle decimalStyle, Row filaEjemplo, Workbook workbook)
            throws SQLException {
        Function<Integer, Cell> getOrCreateCell = (index) -> {
            Cell cell = row.getCell(index);
            if (cell == null) {
                cell = row.createCell(index);
                if (filaEjemplo != null && filaEjemplo.getCell(index) != null) {
                    cell.setCellStyle(filaEjemplo.getCell(index).getCellStyle());
                } else {
                    cell.setCellStyle(decimalStyle);
                }
            }
            return cell;
        };

        int rowNum = row.getRowNum() + 1;

        // Celdas básicas
        getOrCreateCell.apply(0).setCellValue(rs.getInt("item"));
        getOrCreateCell.apply(1).setCellValue(rs.getString("codi"));
        getOrCreateCell.apply(2).setCellValue(rs.getString("codf"));
        getOrCreateCell.apply(3).setCellValue(rs.getString("marc"));

        getOrCreateCell.apply(4).setCellValue(rs.getDouble("cant"));
        getOrCreateCell.apply(5).setCellValue(rs.getDouble("preu"));

        // Celda con fórmula
        Cell cellG = getOrCreateCell.apply(6);
        cellG.setCellFormula("F" + rowNum + "*$G$1");

        getOrCreateCell.apply(7).setCellValue(rs.getDouble("factor"));

        // Más fórmulas
        Cell cellI = getOrCreateCell.apply(8);
        cellI.setCellFormula("G" + rowNum + "*H" + rowNum);

        Cell cellJ = getOrCreateCell.apply(9);
        cellJ.setCellFormula("I" + rowNum + "*$J$1");

        // Texto
        getOrCreateCell.apply(10).setCellValue(rs.getString("clase"));
        getOrCreateCell.apply(11).setCellValue(rs.getString("TipoOC"));
        getOrCreateCell.apply(12).setCellValue(rs.getDate("FechaIng") != null 
            ? new java.text.SimpleDateFormat("dd-MM-yyyy").format(rs.getDate("FechaIng")) 
            : "");
        getOrCreateCell.apply(13).setCellValue(rs.getString("nompro"));
        getOrCreateCell.apply(14).setCellValue(rs.getString("mone"));

        // Valores numéricos
        getOrCreateCell.apply(15).setCellValue(rs.getDouble("cant_kardex"));
        getOrCreateCell.apply(16).setCellValue(rs.getDouble("cpu"));
        getOrCreateCell.apply(17).setCellValue(rs.getDouble("stocSede"));
        getOrCreateCell.apply(18).setCellValue(rs.getDouble("stocLince"));
        getOrCreateCell.apply(19).setCellValue(rs.getDouble("Pfob"));
        getOrCreateCell.apply(20).setCellValue(rs.getDouble("Pgop"));
        getOrCreateCell.apply(21).setCellValue(rs.getDouble("cpa"));
        getOrCreateCell.apply(22).setCellValue(rs.getDouble("factor"));
        getOrCreateCell.apply(23).setCellValue(rs.getDouble("listSis"));

        // Fórmulas avanzadas
        Cell analisisCell = getOrCreateCell.apply(24);
        analisisCell.setCellFormula("IF(ROUND(I" + rowNum + ",3)>ROUND(X" + rowNum + ",3),1,IF(ROUND(I" + rowNum
                + ",3)=ROUND(X" + rowNum + ",3),2,3))");

        Cell listCell = getOrCreateCell.apply(25);
        listCell.setCellFormula("(T" + rowNum + "+U" + rowNum + ")*W" + rowNum);

        Cell diffCell = getOrCreateCell.apply(26);
        diffCell.setCellFormula("Z" + rowNum + "-X" + rowNum);

        Cell pfobDiffCell = getOrCreateCell.apply(27);
        pfobDiffCell.setCellFormula("E" + rowNum + "-T" + rowNum);

        Cell factorDiffCell = getOrCreateCell.apply(28);
        factorDiffCell.setCellFormula("H" + rowNum + "-W" + rowNum);
    }

    private CellStyle createDecimalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.000"));
        return style;
    }
}
