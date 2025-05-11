package com.sudamericana.impoexcel.service;

import com.sudamericana.impoexcel.model.ReposicionFiltro;
import com.sudamericana.impoexcel.model.ReposicionProducto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReposicionProductoService {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ReposicionProductoService.class);

    @Autowired
    public ReposicionProductoService(@Qualifier("sqlServerDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<ReposicionProducto> buscarReposicionProductos(ReposicionFiltro filtro) {
        try {
            String estados = filtro.getEstadosClausulaWhere();

            logger.info(
                    "Ejecutando SP_Reporte_Reposiciones_cambios_V5 con parámetros: meses={}, marca={}, diasProv={}, factorSeg={}, codalm={}, estados={}",
                    filtro.getMesesVenta(), filtro.getMarca(), filtro.getDiasDemoraProveedor(),
                    filtro.getFactorDiasSeguridad(), filtro.getCodAlm(), estados);

            return jdbcTemplate.query(
                    "{call SP_Reporte_Reposiciones_cambios_V5(?, ?, ?, ?, ?, ?)}",
                    new Object[] {
                            filtro.getMesesVenta(),
                            filtro.getMarca(),
                            filtro.getDiasDemoraProveedor(),
                            filtro.getFactorDiasSeguridad(),
                            filtro.getCodAlm(),
                            estados
                    },
                    new ReposicionProductoRowMapper());

        } catch (DataAccessException e) {
            logger.error("Error al ejecutar SP_Reporte_Reposiciones_cambios_V5: {}", e.getMessage(), e);
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error general: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private class ReposicionProductoRowMapper implements RowMapper<ReposicionProducto> {

        private Set<String> availableColumns = null;
        private Set<String> availableColumnsCaseInsensitive = null;
        private java.util.Map<String, String> columnNameMapping = null;

        @Override
        public ReposicionProducto mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                if (availableColumns == null) {
                    initializeColumnMappings(rs);
                    logAvailableColumns(availableColumns);
                }

                ReposicionProducto producto = new ReposicionProducto();

                setString(rs, "clase", producto::setClase);
                setString(rs, "codi", producto::setCodi);
                setString(rs, "codf", producto::setCodf);
                setString(rs, "descr", producto::setDescripcion);
                setString(rs, "marc", producto::setMarca);
                setBigDecimal(rs, "cantvendida", producto::setCantidadVendida);
                setBigDecimal(rs, "cantventos", producto::setCantVentOS);
                setInt(rs, "mesesventas", producto::setMesesVentas);
                setInt(rs, "vecesventa", producto::setNroVentas);
                setBigDecimal(rs, "porcmes", producto::setPorcentajeMes);
                setBigDecimal(rs, "porcdia", producto::setPorcentajeDia);
                if (availableColumnsCaseInsensitive.contains("01")) {
                    setBigDecimal(rs, "01", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("02")) {
                    setBigDecimal(rs, "02", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("03")){
                    setBigDecimal(rs, "03", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("04")){
                    setBigDecimal(rs, "04", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("05")){
                    setBigDecimal(rs, "05", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("06")){
                    setBigDecimal(rs, "06", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("07")){
                    setBigDecimal(rs, "07", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("08")){
                    setBigDecimal(rs, "08", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("09")){
                    setBigDecimal(rs, "09", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("10")){
                    setBigDecimal(rs, "10", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("11")){
                    setBigDecimal(rs, "11", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("12")){
                    setBigDecimal(rs, "12", producto::setStockTotal);
                } if (availableColumnsCaseInsensitive.contains("stoc")){
                    setBigDecimal(rs, "stoc", producto::setStockTotal);
                } else {
                    setBigDecimal(rs, "11", producto::setStockTotal);
                }
                setBigDecimal(rs, "stockIni", producto::setStockIni);
                setBigDecimal(rs, "cantsegura", producto::setFactorSeguridad);
                setBigDecimal(rs, "cantreponer", producto::setCantidadReponer);
                setString(rs, "estado", producto::setEstado);
                setString(rs, "fecultventas", producto::setFechaUltimaVenta);
                setString(rs, "fecultventas", producto::setFechaUltimaVenta);
                setString(rs, "fecultventa", producto::setFechaUltimaVenta);
                setBigDecimal(rs, "upc", producto::setUltimoPrecioCompra);
                setString(rs, "orden", producto::setUltimaOrdenCompra);
                setString(rs, "fechaing", producto::setFechaIngresoAlmacen);
                setString(rs, "nompro", producto::setProveedor);

                // Campos opcionales
                setString(rs, "tipooc", producto::setGlosa);
                setString(rs, "fechatrans", producto::setFechaTransferencia);

                // Stocks por almacén (00 y 01)
                // Para Lince
                if (availableColumnsCaseInsensitive.contains("stoclince")) {
                    setBigDecimal(rs, "stoclince", producto::setStockLince);
                } else {
                    setBigDecimal(rs, "01", producto::setStockLince);
                }

                // Para San Luis
                if (availableColumnsCaseInsensitive.contains("stocsnluis")) {
                    setBigDecimal(rs, "stocsnluis", producto::setStockSanLuis);
                } else {
                    setBigDecimal(rs, "03", producto::setStockSanLuis);
                }

                // Stocks por almacén
                setBigDecimal(rs, "02", producto::setStockArequipa);
                setBigDecimal(rs, "otroAlm", producto::setOtroAlm);
                setBigDecimal(rs, "04", producto::setStockTrujillo);
                setBigDecimal(rs, "05", producto::setStockCerroColorado);
                setBigDecimal(rs, "06", producto::setStockLosOlivos);
                setBigDecimal(rs, "07", producto::setStockChiclayo);
                setBigDecimal(rs, "08", producto::setStockMercaderiaObsoletaSanLuis);
                setBigDecimal(rs, "10", producto::setStockTomocorp1);
                setBigDecimal(rs, "12", producto::setStockObsoletaBajaMercaderia);
                setBigDecimal(rs, "13", producto::setStockMercaderiaObsoletaArequipa);
                setBigDecimal(rs, "transito", producto::setStockTransito);

                return producto;
            } catch (SQLException e) {
                logger.error("Error al mapear la fila {}: {}", rowNum, e.getMessage());
                throw e;
            }
        }

        private void initializeColumnMappings(ResultSet rs) throws SQLException {
            availableColumns = new HashSet<>();
            availableColumnsCaseInsensitive = new HashSet<>();
            columnNameMapping = new java.util.HashMap<>();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            StringBuilder detailedColumns = new StringBuilder("Detalle de columnas disponibles:\n");

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                availableColumns.add(columnName);

                String lowerColumnName = columnName.toLowerCase();
                availableColumnsCaseInsensitive.add(lowerColumnName);
                columnNameMapping.put(lowerColumnName, columnName);

                detailedColumns.append(i)
                        .append(". Nombre: ")
                        .append(columnName)
                        .append(", Tipo: ")
                        .append(metaData.getColumnTypeName(i))
                        .append(", Class: ")
                        .append(metaData.getColumnClassName(i))
                        .append("\n");
            }

            logger.info(detailedColumns.toString());
        }

        private void logAvailableColumns(Set<String> columns) {
            StringBuilder sb = new StringBuilder("Columnas disponibles: ");
            for (String column : columns) {
                sb.append(column).append(", ");
            }
            logger.info(sb.toString());
        }

        private void setString(ResultSet rs, String columnName, StringSetter setter) {
            String lowerColumnName = columnName.toLowerCase();
            if (availableColumnsCaseInsensitive.contains(lowerColumnName)) {
                try {
                    String actualColumnName = columnNameMapping.get(lowerColumnName);
                    String value = rs.getString(actualColumnName);
                    if (value != null) {
                        setter.set(value);
                    }
                } catch (SQLException e) {
                    logger.debug("Error al obtener la columna {}: {}", columnName, e.getMessage());
                }
            }
        }

        private void setBigDecimal(ResultSet rs, String columnName, BigDecimalSetter setter) {
            String lowerColumnName = columnName.toLowerCase();
            if (availableColumnsCaseInsensitive.contains(lowerColumnName)) {
                try {
                    String actualColumnName = columnNameMapping.get(lowerColumnName);
                    BigDecimal value = rs.getBigDecimal(actualColumnName);
                    if (value != null) {
                        setter.set(value);
                    }
                } catch (SQLException e) {
                    logger.debug("Error al obtener la columna {}: {}", columnName, e.getMessage());
                }
            }
        }

        private void setInt(ResultSet rs, String columnName, IntSetter setter) {
            String lowerColumnName = columnName.toLowerCase();
            if (availableColumnsCaseInsensitive.contains(lowerColumnName)) {
                try {
                    String actualColumnName = columnNameMapping.get(lowerColumnName);
                    int value = rs.getInt(actualColumnName);
                    if (!rs.wasNull()) {
                        setter.set(value);
                    }
                } catch (SQLException e) {
                    logger.debug("Error al obtener la columna {}: {}", columnName, e.getMessage());
                }
            }
        }
    }

    @FunctionalInterface
    private interface StringSetter {
        void set(String value);
    }

    @FunctionalInterface
    private interface BigDecimalSetter {
        void set(BigDecimal value);
    }

    @FunctionalInterface
    private interface IntSetter {
        void set(int value);
    }
}