package com.sudamericana.impoexcel.service;

import com.sudamericana.impoexcel.model.ReporteABC;
import com.sudamericana.impoexcel.model.ReporteABCFiltro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReporteABCService {

    private static final Logger logger = LoggerFactory.getLogger(ReporteABCService.class);

    private final JdbcTemplate jdbcTemplate;
    private SimpleJdbcCall simpleJdbcCall;

    @Autowired
    public ReporteABCService(@Qualifier("sqlServerDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        RowMapper<ReporteABC> reporteRowMapper = new RowMapper<ReporteABC>() {
            @Override
            public ReporteABC mapRow(ResultSet rs, int rowNum) throws SQLException {
                ReporteABC reporte = new ReporteABC();

                reporte.setCurva(rs.getString("CURVA"));
                reporte.setCodCli(rs.getString("CODCLI"));
                reporte.setCliente(rs.getString("CLIENTE"));
                reporte.setRuccli(rs.getString("RUCCLI"));
                reporte.setDiaz(rs.getInt("DIAZ"));
                reporte.setCantidad(rs.getBigDecimal("CANTIDAD"));
                reporte.setMonto(rs.getBigDecimal("MONTO"));
                reporte.setPorcAcumulada(rs.getString("PORC_ACUMULADA"));
                reporte.setDesvendedor(rs.getString("DESVENDEDOR"));
                reporte.setDesestado(rs.getString("DESESTADO"));
                reporte.setLimiteCredito(rs.getBigDecimal("LIMITE_CREDITO"));
                reporte.setAprobacion(rs.getString("APROBACION"));
                reporte.setCondicionPago(rs.getString("DESCDVENTA"));
                reporte.setDesdepartamento(rs.getString("DESDEPARTAMENTO"));
                reporte.setDistrito(rs.getString("DISTRITO"));
                reporte.setTelefono(rs.getString("TELEFONO"));

                return reporte;
            }
        };

        this.simpleJdbcCall = new SimpleJdbcCall(dataSource)
                .withSchemaName("FACT")
                .withProcedureName("RPT_VENTAS")
                .returningResultSet("reportes", reporteRowMapper);
    }

    public List<ReporteABC> generarReporteABC(ReporteABCFiltro filtro) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        SqlParameterSource paramMap = new MapSqlParameterSource()
                .addValue("LITIPSQL", 1)
                .addValue("LSFECINI", filtro.getFechaInicio().format(formatter))
                .addValue("LSFECFIN", filtro.getFechaFin().format(formatter))
                .addValue("LSPARAM1", filtro.getVendedor())
                .addValue("LSPARAM2", filtro.getEstado())
                .addValue("LSPARAM3", filtro.getDuvDesde())
                .addValue("LSPARAM4", filtro.getDuvHasta())
                .addValue("LSPARAM5", filtro.getCondicionPago());

        Map<String, Object> result = simpleJdbcCall.execute(paramMap);

        @SuppressWarnings("unchecked")
        List<ReporteABC> reportes = (List<ReporteABC>) result.get("reportes");

        return reportes;
    }
    // producto

    public List<ReporteABC> generarReporteABCProducto(ReporteABCFiltro filtro) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            if (filtro.getFechaInicio() == null || filtro.getFechaFin() == null) {
                logger.error("Fechas de inicio o fin no especificadas");
                return new ArrayList<>();
            }

            String fechaInicio = filtro.getFechaInicio().format(formatter);
            String fechaFin = filtro.getFechaFin().format(formatter);

            String pctCantidad = "20";
            String pctMovimiento = "40";
            String pctVenta = "40";
            if (filtro.getPctCantidad() != null) {
                pctCantidad = filtro.getPctCantidad().replace(".0", "");
            }
            if (filtro.getPctMovimiento() != null) {
                pctMovimiento = filtro.getPctMovimiento().replace(".0", "");
            }
            if (filtro.getPctVenta() != null) {
                pctVenta = filtro.getPctVenta().replace(".0", "");
            }

            String pesos = pctCantidad + ";" + pctMovimiento + ";" + pctVenta + ";";

            String curvaA = "50";
            String curvaB = "65";
            String curvaC = "100";

            if (filtro.getCurvaA() != null) {
                curvaA = filtro.getCurvaA().replace(".0", "");
            }
            if (filtro.getCurvaB() != null) {
                curvaB = filtro.getCurvaB().replace(".0", "");
            }
            if (filtro.getCurvaC() != null) {
                curvaC = filtro.getCurvaC().replace(".0", "");
            }

            String curvas = curvaA + ";" + curvaB + ";" + curvaC + ";";

            String marca = filtro.getMarca() != null ? filtro.getMarca() : "%";

            logger.info("Ejecutando SP con parámetros optimizados:");
            logger.info("  Tipo = 1 (Productos)");
            logger.info("  Fecha inicio = {}", fechaInicio);
            logger.info("  Fecha fin = {}", fechaFin);
            logger.info("  Marca = {}", marca);
            logger.info("  Pesos = {}", pesos);
            logger.info("  Curvas = {}", curvas);
            logger.info("  Almacén = %");

            List<ReporteABC> reportes = jdbcTemplate.query(
                    "{call FACT.RPT_VENTAS2(?, ?, ?, ?, ?, ?, ?, ?)}",
                    new PreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps) throws SQLException {
                            ps.setInt(1, 1); // LITIPSQL = 1 (Productos)
                            ps.setString(2, fechaInicio); // LSFECINI
                            ps.setString(3, fechaFin); // LSFECFIN
                            ps.setString(4, marca); // LSPARAM1 (Marca)
                            ps.setString(5, pesos); // LSPARAM2 (Pesos)
                            ps.setString(6, curvas); // LSPARAM3 (Curvas ABC)
                            ps.setString(7, "%"); // LSPARAM4 (Almacén) - debe ser "%"
                            ps.setString(8, ""); // LSPARAM5 (No usado)
                        }
                    },
                    new RowMapper<ReporteABC>() {
                        @Override
                        public ReporteABC mapRow(ResultSet rs, int rowNum) throws SQLException {
                            ReporteABC reporte = new ReporteABC();

                            if (rowNum == 0) {
                                ResultSetMetaData metaData = rs.getMetaData();
                                int columnCount = metaData.getColumnCount();

                                StringBuilder columnas = new StringBuilder("Columnas en el resultset: ");

                                boolean tieneTC = false;
                                boolean tieneTM = false;
                                boolean tieneC = false;
                                boolean tieneM = false;

                                for (int i = 1; i <= columnCount; i++) {
                                    String colName = metaData.getColumnName(i);
                                    columnas.append(colName).append(", ");

                                    if (colName.equals("TC"))
                                        tieneTC = true;
                                    if (colName.equals("TM"))
                                        tieneTM = true;
                                    if (colName.matches("C\\d{2}"))
                                        tieneC = true;
                                    if (colName.matches("M\\d{2}"))
                                        tieneM = true;
                                }

                                logger.info(columnas.toString());
                                logger.info("Patrones de columnas: TC={}, TM={}, C##={}, M##={}",
                                        tieneTC, tieneTM, tieneC, tieneM);
                            }

                            if (rowNum < 3 || rowNum % 100 == 0) {
                                ResultSetMetaData metaData;
                                try {
                                    metaData = rs.getMetaData();
                                    int columnCount = metaData.getColumnCount();
                                    StringBuilder values = new StringBuilder();
                                    values.append("Fila ").append(rowNum).append(": ");

                                    for (int i = 1; i <= columnCount; i++) {
                                        String colName = metaData.getColumnName(i);
                                        String value = rs.getString(i);
                                        values.append(colName).append("=").append(value).append(", ");
                                    }
                                    logger.debug(values.toString());
                                } catch (SQLException e) {
                                    logger.error("Error al obtener metadatos", e);
                                }
                            }

                            try {
                                reporte.setCurva(getValue(rs, "CURVA", String.class));
                                reporte.setCodProducto(getValue(rs, "PRODUCTO", String.class));
                                reporte.setProducto(getValue(rs, "DESCRIPCION", String.class));
                                reporte.setDescripcion1(getValue(rs, "DESCRIPCION1", String.class));
                                reporte.setMarca(getValue(rs, "MARC", String.class));
                                reporte.setStock(getValue(rs, "STOCK", BigDecimal.class));
                                reporte.setUnidad(getValue(rs, "UNIDAD", String.class));

                                BigDecimal tc = getValue(rs, "TC", BigDecimal.class);
                                BigDecimal tv = getValue(rs, "TV", BigDecimal.class);
                                String linea = getValue(rs, "LINEA", String.class);

                                reporte.setCantidad(tc);
                                reporte.setMonto(tv != null ? tv : tc);
                                reporte.setPorcAcumulada(linea);

                                reporte.setCantidadEne(getMonthValue(rs, "C01", "CE1", "CENE", "QE1", "mes1"));
                                reporte.setCantidadFeb(getMonthValue(rs, "C02", "CE2", "CFEB", "QE2", "mes2"));
                                reporte.setCantidadMar(getMonthValue(rs, "C03", "CE3", "CMAR", "QE3", "mes3"));
                                reporte.setCantidadAbr(getMonthValue(rs, "C04", "CE4", "CABR", "QE4", "mes4"));
                                reporte.setCantidadMay(getMonthValue(rs, "C05", "CE5", "CMAY", "QE5", "mes5"));
                                reporte.setCantidadJun(getMonthValue(rs, "C06", "CE6", "CJUN", "QE6", "mes6"));
                                reporte.setCantidadJul(getMonthValue(rs, "C07", "CE7", "CJUL", "QE7", "mes7"));
                                reporte.setCantidadAgo(getMonthValue(rs, "C08", "CE8", "CAGO", "QE8", "mes8"));
                                reporte.setCantidadSet(getMonthValue(rs, "C09", "CE9", "CSET", "QE9", "mes9"));
                                reporte.setCantidadOct(getMonthValue(rs, "C10", "CE10", "COCT", "QE10", "mes10"));
                                reporte.setCantidadNov(getMonthValue(rs, "C11", "CE11", "CNOV", "QE11", "mes11"));
                                reporte.setCantidadDic(getMonthValue(rs, "C12", "CE12", "CDIC", "QE12", "mes12"));

                                if (reporte.getCantidadEne() == null)
                                    reporte.setCantidadEne(getValue(rs, "CENE", BigDecimal.class));
                                if (reporte.getCantidadFeb() == null)
                                    reporte.setCantidadFeb(getValue(rs, "CFEB", BigDecimal.class));
                                if (reporte.getCantidadMar() == null)
                                    reporte.setCantidadMar(getValue(rs, "CMAR", BigDecimal.class));
                                if (reporte.getCantidadAbr() == null)
                                    reporte.setCantidadAbr(getValue(rs, "CABR", BigDecimal.class));
                                if (reporte.getCantidadMay() == null)
                                    reporte.setCantidadMay(getValue(rs, "CMAY", BigDecimal.class));
                                if (reporte.getCantidadJun() == null)
                                    reporte.setCantidadJun(getValue(rs, "CJUN", BigDecimal.class));
                                if (reporte.getCantidadJul() == null)
                                    reporte.setCantidadJul(getValue(rs, "CJUL", BigDecimal.class));
                                if (reporte.getCantidadAgo() == null)
                                    reporte.setCantidadAgo(getValue(rs, "CAGO", BigDecimal.class));
                                if (reporte.getCantidadSet() == null)
                                    reporte.setCantidadSet(getValue(rs, "CSET", BigDecimal.class));
                                if (reporte.getCantidadOct() == null)
                                    reporte.setCantidadOct(getValue(rs, "COCT", BigDecimal.class));
                                if (reporte.getCantidadNov() == null)
                                    reporte.setCantidadNov(getValue(rs, "CNOV", BigDecimal.class));
                                if (reporte.getCantidadDic() == null)
                                    reporte.setCantidadDic(getValue(rs, "CDIC", BigDecimal.class));

                                if (reporte.getCantidadEne() == null)
                                    reporte.setCantidadEne(getValue(rs, "QE1", BigDecimal.class));
                                if (reporte.getCantidadFeb() == null)
                                    reporte.setCantidadFeb(getValue(rs, "QE2", BigDecimal.class));
                                if (reporte.getCantidadMar() == null)
                                    reporte.setCantidadMar(getValue(rs, "QE3", BigDecimal.class));
                                if (reporte.getCantidadAbr() == null)
                                    reporte.setCantidadAbr(getValue(rs, "QE4", BigDecimal.class));
                                if (reporte.getCantidadMay() == null)
                                    reporte.setCantidadMay(getValue(rs, "QE5", BigDecimal.class));
                                if (reporte.getCantidadJun() == null)
                                    reporte.setCantidadJun(getValue(rs, "QE6", BigDecimal.class));
                                if (reporte.getCantidadJul() == null)
                                    reporte.setCantidadJul(getValue(rs, "QE7", BigDecimal.class));
                                if (reporte.getCantidadAgo() == null)
                                    reporte.setCantidadAgo(getValue(rs, "QE8", BigDecimal.class));
                                if (reporte.getCantidadSet() == null)
                                    reporte.setCantidadSet(getValue(rs, "QE9", BigDecimal.class));
                                if (reporte.getCantidadOct() == null)
                                    reporte.setCantidadOct(getValue(rs, "QE10", BigDecimal.class));
                                if (reporte.getCantidadNov() == null)
                                    reporte.setCantidadNov(getValue(rs, "QE11", BigDecimal.class));
                                if (reporte.getCantidadDic() == null)
                                    reporte.setCantidadDic(getValue(rs, "QE12", BigDecimal.class));

                                reporte.setMontoEne(getMonthValue(rs, "M01", "ME1", "MENE", "VE1", "monto1"));
                                reporte.setMontoFeb(getMonthValue(rs, "M02", "ME2", "MFEB", "VE2", "monto2"));
                                reporte.setMontoMar(getMonthValue(rs, "M03", "ME3", "MMAR", "VE3", "monto3"));
                                reporte.setMontoAbr(getMonthValue(rs, "M04", "ME4", "MABR", "VE4", "monto4"));
                                reporte.setMontoMay(getMonthValue(rs, "M05", "ME5", "MMAY", "VE5", "monto5"));
                                reporte.setMontoJun(getMonthValue(rs, "M06", "ME6", "MJUN", "VE6", "monto6"));
                                reporte.setMontoJul(getMonthValue(rs, "M07", "ME7", "MJUL", "VE7", "monto7"));
                                reporte.setMontoAgo(getMonthValue(rs, "M08", "ME8", "MAGO", "VE8", "monto8"));
                                reporte.setMontoSet(getMonthValue(rs, "M09", "ME9", "MSET", "VE9", "monto9"));
                                reporte.setMontoOct(getMonthValue(rs, "M10", "ME10", "MOCT", "VE10", "monto10"));
                                reporte.setMontoNov(getMonthValue(rs, "M11", "ME11", "MNOV", "VE11", "monto11"));
                                reporte.setMontoDic(getMonthValue(rs, "M12", "ME12", "MDIC", "VE12", "monto12"));

                                if (reporte.getCantidadEne() == null && reporte.getCantidadFeb() == null &&
                                        reporte.getCantidadMar() == null && reporte.getCantidadAbr() == null &&
                                        reporte.getCantidadMay() == null && reporte.getCantidadJun() == null &&
                                        reporte.getCantidadJul() == null && reporte.getCantidadAgo() == null &&
                                        reporte.getCantidadSet() == null && reporte.getCantidadOct() == null &&
                                        reporte.getCantidadNov() == null && reporte.getCantidadDic() == null) {

                                    int mesActual = LocalDate.now().getMonthValue();
                                    switch (mesActual) {
                                        case 1:
                                            reporte.setCantidadEne(tc);
                                            reporte.setMontoEne(tv);
                                            break;
                                        case 2:
                                            reporte.setCantidadFeb(tc);
                                            reporte.setMontoFeb(tv);
                                            break;
                                        case 3:
                                            reporte.setCantidadMar(tc);
                                            reporte.setMontoMar(tv);
                                            break;
                                        case 4:
                                            reporte.setCantidadAbr(tc);
                                            reporte.setMontoAbr(tv);
                                            break;
                                        case 5:
                                            reporte.setCantidadMay(tc);
                                            reporte.setMontoMay(tv);
                                            break;
                                        case 6:
                                            reporte.setCantidadJun(tc);
                                            reporte.setMontoJun(tv);
                                            break;
                                        case 7:
                                            reporte.setCantidadJul(tc);
                                            reporte.setMontoJul(tv);
                                            break;
                                        case 8:
                                            reporte.setCantidadAgo(tc);
                                            reporte.setMontoAgo(tv);
                                            break;
                                        case 9:
                                            reporte.setCantidadSet(tc);
                                            reporte.setMontoSet(tv);
                                            break;
                                        case 10:
                                            reporte.setCantidadOct(tc);
                                            reporte.setMontoOct(tv);
                                            break;
                                        case 11:
                                            reporte.setCantidadNov(tc);
                                            reporte.setMontoNov(tv);
                                            break;
                                        case 12:
                                            reporte.setCantidadDic(tc);
                                            reporte.setMontoDic(tv);
                                            break;
                                    }
                                }

                                reporte.setFechaHora(getValue(rs, "FECHA_HORA", String.class));
                                reporte.setUsuario(getValue(rs, "USUARIO", String.class));
                                reporte.setTitulo(getValue(rs, "TITULO", String.class));
                                reporte.setRptTitulo(getValue(rs, "RPT_TITULO", String.class));
                                reporte.setFuente(getValue(rs, "FUENTE", String.class));

                                if (reporte.getCurva() == null)
                                    reporte.setCurva("N");
                                if (reporte.getDesestado() == null)
                                    reporte.setDesestado("Activo");
                                if (reporte.getDiaz() == null)
                                    reporte.setDiaz(0);
                            } catch (Exception e) {
                                logger.error("Error al mapear fila {}: {}", rowNum, e.getMessage());
                            }

                            return reporte;
                        }
                    });

            boolean necesitaEnriquecimiento = false;
            if (!reportes.isEmpty()) {
                ReporteABC muestra = reportes.get(0);
                logger.info("Valores mensuales encontrados en el primer registro:");
                logger.info("  ENE: {}/{}", muestra.getCantidadEne(), muestra.getMontoEne());
                logger.info("  FEB: {}/{}", muestra.getCantidadFeb(), muestra.getMontoFeb());
                logger.info("  MAR: {}/{}", muestra.getCantidadMar(), muestra.getMontoMar());

                if ((muestra.getCantidadEne() == null || muestra.getCantidadEne().compareTo(BigDecimal.ZERO) == 0) &&
                        (muestra.getCantidadFeb() == null || muestra.getCantidadFeb().compareTo(BigDecimal.ZERO) == 0)
                        &&
                        (muestra.getCantidadMar() == null
                                || muestra.getCantidadMar().compareTo(BigDecimal.ZERO) == 0)) {

                    necesitaEnriquecimiento = true;
                    logger.info("Los datos mensuales no están disponibles en el SP. Intentando enriquecerlos.");
                } else {
                    logger.info("Los datos mensuales ya están disponibles en el SP, no se necesita enriquecimiento.");
                }
            }

            if (necesitaEnriquecimiento && !reportes.isEmpty()) {
                int año = filtro.getFechaInicio().getYear();
                reportes = enriquecerConDatosMensuales(reportes, año);
            }

            return reportes;
        } catch (DataAccessException e) {
            logger.error("Error de acceso a datos: {}", e.getMessage());
            logger.error("Causa: ", e);
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error general: {}", e.getMessage());
            logger.error("Causa: ", e);
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getValue(ResultSet rs, String columnName, Class<T> type) {
        try {
            if (rs.findColumn(columnName) > 0) {
                if (type == String.class) {
                    String value = rs.getString(columnName);
                    return rs.wasNull() ? null : (T) value;
                } else if (type == BigDecimal.class) {
                    BigDecimal value = rs.getBigDecimal(columnName);
                    return rs.wasNull() ? null : (T) value;
                } else if (type == Integer.class) {
                    int value = rs.getInt(columnName);
                    return rs.wasNull() ? null : (T) Integer.valueOf(value);
                }
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    private BigDecimal getMonthValue(ResultSet rs, String... columnNames) {
        for (String columnName : columnNames) {
            BigDecimal value = getValue(rs, columnName, BigDecimal.class);
            if (value != null) {
                return value;
            }
        }
        return BigDecimal.ZERO;
    }

    private List<ReporteABC> enriquecerConDatosMensuales(List<ReporteABC> reportesBase, int año) {
        if (reportesBase == null || reportesBase.isEmpty()) {
            logger.warn("No hay reportes para enriquecer con datos mensuales");
            return reportesBase;
        }

        try {
            logger.info("Enriqueciendo {} reportes con datos mensuales del año {}", reportesBase.size(), año);

            StringBuilder productosStr = new StringBuilder();
            int contador = 0;
            for (ReporteABC r : reportesBase) {
                if (r.getCodProducto() != null && !r.getCodProducto().trim().isEmpty()) {
                    if (contador > 0) {
                        productosStr.append(",");
                    }
                    productosStr.append("'").append(r.getCodProducto().trim()).append("'");
                    contador++;

                    if (contador >= 300) {
                        break;
                    }
                }
            }

            if (productosStr.length() == 0) {
                logger.warn("No se encontraron códigos de producto válidos para consultar");
                return reportesBase;
            }

            String query = "SELECT " +
                    "    d.PRODUCTO, " +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 1 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_ene, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 1 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_ene, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 2 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_feb, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 2 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_feb, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 3 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_mar, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 3 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_mar, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 4 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_abr, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 4 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_abr, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 5 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_may, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 5 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_may, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 6 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_jun, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 6 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_jun, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 7 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_jul, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 7 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_jul, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 8 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_ago, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 8 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_ago, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 9 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_set, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 9 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_set, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 10 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_oct, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 10 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_oct, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 11 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_nov, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 11 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_nov, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 12 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD ELSE 0 END) AS cant_dic, "
                    +
                    "    SUM(CASE WHEN MONTH(c.FECHA) = 12 AND YEAR(c.FECHA) = ? THEN d.CANTIDAD * d.PRECIO ELSE 0 END) AS monto_dic "
                    +
                    "FROM BdNava01.dbo.Mov0101 d " +
                    "JOIN BdNava01.dbo.Mov0100 c ON d.TIPODOC = c.TIPODOC AND d.NUMDOC = c.NUMDOC " +
                    "WHERE d.PRODUCTO IN (" + productosStr.toString() + ") " +
                    "    AND c.TIPODOC IN ('BO','FA') " +
                    "    AND c.ANULADO = 0 " +
                    "    AND YEAR(c.FECHA) = ? " +
                    "GROUP BY d.PRODUCTO";

            Object[] params = new Object[25];
            for (int i = 0; i < 24; i++) {
                params[i] = año;
            }
            params[24] = año;

            logger.info("Ejecutando consulta para datos mensuales: {}",
                    query.substring(0, Math.min(query.length(), 200)) + "...");

            // Ejecutar la consulta
            final java.util.concurrent.ConcurrentHashMap<String, ReporteABC> datosMensuales = new java.util.concurrent.ConcurrentHashMap<>();

            jdbcTemplate.query(
                    query,
                    params,
                    rs -> {
                        String codigo = rs.getString("PRODUCTO");
                        if (codigo != null) {
                            ReporteABC datos = new ReporteABC();

                            // Mapear datos mensuales
                            datos.setCantidadEne(rs.getBigDecimal("cant_ene"));
                            datos.setMontoEne(rs.getBigDecimal("monto_ene"));
                            datos.setCantidadFeb(rs.getBigDecimal("cant_feb"));
                            datos.setMontoFeb(rs.getBigDecimal("monto_feb"));
                            datos.setCantidadMar(rs.getBigDecimal("cant_mar"));
                            datos.setMontoMar(rs.getBigDecimal("monto_mar"));
                            datos.setCantidadAbr(rs.getBigDecimal("cant_abr"));
                            datos.setMontoAbr(rs.getBigDecimal("monto_abr"));
                            datos.setCantidadMay(rs.getBigDecimal("cant_may"));
                            datos.setMontoMay(rs.getBigDecimal("monto_may"));
                            datos.setCantidadJun(rs.getBigDecimal("cant_jun"));
                            datos.setMontoJun(rs.getBigDecimal("monto_jun"));
                            datos.setCantidadJul(rs.getBigDecimal("cant_jul"));
                            datos.setMontoJul(rs.getBigDecimal("monto_jul"));
                            datos.setCantidadAgo(rs.getBigDecimal("cant_ago"));
                            datos.setMontoAgo(rs.getBigDecimal("monto_ago"));
                            datos.setCantidadSet(rs.getBigDecimal("cant_set"));
                            datos.setMontoSet(rs.getBigDecimal("monto_set"));
                            datos.setCantidadOct(rs.getBigDecimal("cant_oct"));
                            datos.setMontoOct(rs.getBigDecimal("monto_oct"));
                            datos.setCantidadNov(rs.getBigDecimal("cant_nov"));
                            datos.setMontoNov(rs.getBigDecimal("monto_nov"));
                            datos.setCantidadDic(rs.getBigDecimal("cant_dic"));
                            datos.setMontoDic(rs.getBigDecimal("monto_dic"));

                            datosMensuales.put(codigo.trim(), datos);

                            logger.debug("Datos para producto {}: ENE={}/{}, FEB={}/{}, MAR={}/{}",
                                    codigo,
                                    datos.getCantidadEne(), datos.getMontoEne(),
                                    datos.getCantidadFeb(), datos.getMontoFeb(),
                                    datos.getCantidadMar(), datos.getMontoMar());
                        }
                    });

            logger.info("Datos mensuales encontrados para {} productos", datosMensuales.size());

            int productosEnriquecidos = 0;
            for (ReporteABC reporte : reportesBase) {
                if (reporte.getCodProducto() != null) {
                    String codigo = reporte.getCodProducto().trim();
                    ReporteABC datosMes = datosMensuales.get(codigo);

                    if (datosMes != null) {
                        if (reporte.getCantidadEne() == null
                                || reporte.getCantidadEne().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadEne(datosMes.getCantidadEne());
                        }
                        if (reporte.getMontoEne() == null || reporte.getMontoEne().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoEne(datosMes.getMontoEne());
                        }

                        if (reporte.getCantidadFeb() == null
                                || reporte.getCantidadFeb().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadFeb(datosMes.getCantidadFeb());
                        }
                        if (reporte.getMontoFeb() == null || reporte.getMontoFeb().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoFeb(datosMes.getMontoFeb());
                        }

                        if (reporte.getCantidadMar() == null
                                || reporte.getCantidadMar().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadMar(datosMes.getCantidadMar());
                        }
                        if (reporte.getMontoMar() == null || reporte.getMontoMar().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoMar(datosMes.getMontoMar());
                        }

                        if (reporte.getCantidadAbr() == null
                                || reporte.getCantidadAbr().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadAbr(datosMes.getCantidadAbr());
                        }
                        if (reporte.getMontoAbr() == null || reporte.getMontoAbr().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoAbr(datosMes.getMontoAbr());
                        }

                        if (reporte.getCantidadMay() == null
                                || reporte.getCantidadMay().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadMay(datosMes.getCantidadMay());
                        }
                        if (reporte.getMontoMay() == null || reporte.getMontoMay().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoMay(datosMes.getMontoMay());
                        }

                        if (reporte.getCantidadJun() == null
                                || reporte.getCantidadJun().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadJun(datosMes.getCantidadJun());
                        }
                        if (reporte.getMontoJun() == null || reporte.getMontoJun().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoJun(datosMes.getMontoJun());
                        }

                        if (reporte.getCantidadJul() == null
                                || reporte.getCantidadJul().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadJul(datosMes.getCantidadJul());
                        }
                        if (reporte.getMontoJul() == null || reporte.getMontoJul().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoJul(datosMes.getMontoJul());
                        }

                        if (reporte.getCantidadAgo() == null
                                || reporte.getCantidadAgo().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadAgo(datosMes.getCantidadAgo());
                        }
                        if (reporte.getMontoAgo() == null || reporte.getMontoAgo().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoAgo(datosMes.getMontoAgo());
                        }

                        if (reporte.getCantidadSet() == null
                                || reporte.getCantidadSet().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadSet(datosMes.getCantidadSet());
                        }
                        if (reporte.getMontoSet() == null || reporte.getMontoSet().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoSet(datosMes.getMontoSet());
                        }

                        if (reporte.getCantidadOct() == null
                                || reporte.getCantidadOct().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadOct(datosMes.getCantidadOct());
                        }
                        if (reporte.getMontoOct() == null || reporte.getMontoOct().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoOct(datosMes.getMontoOct());
                        }

                        if (reporte.getCantidadNov() == null
                                || reporte.getCantidadNov().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadNov(datosMes.getCantidadNov());
                        }
                        if (reporte.getMontoNov() == null || reporte.getMontoNov().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoNov(datosMes.getMontoNov());
                        }

                        if (reporte.getCantidadDic() == null
                                || reporte.getCantidadDic().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setCantidadDic(datosMes.getCantidadDic());
                        }
                        if (reporte.getMontoDic() == null || reporte.getMontoDic().compareTo(BigDecimal.ZERO) == 0) {
                            reporte.setMontoDic(datosMes.getMontoDic());
                        }

                        productosEnriquecidos++;
                    }
                }
            }

            logger.info("{} de {} productos fueron enriquecidos con datos mensuales",
                    productosEnriquecidos, reportesBase.size());
        } catch (Exception e) {
            logger.error("Error al enriquecer con datos mensuales: {}", e.getMessage(), e);
        }

        return reportesBase;
    }
}
