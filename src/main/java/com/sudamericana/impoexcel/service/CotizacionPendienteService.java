package com.sudamericana.impoexcel.service;

import com.sudamericana.impoexcel.model.CotizacionPendiente;
import com.sudamericana.impoexcel.model.CotizacionPendienteFiltro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CotizacionPendienteService {
    
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(CotizacionPendienteService.class);
    
    @Autowired
    public CotizacionPendienteService(@Qualifier("sqlServerDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public List<CotizacionPendiente> buscarCotizacionesPendientes(CotizacionPendienteFiltro filtro) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String fechaIni = filtro.getFechaInicio().format(formatter);
            String fechaFin = filtro.getFechaFin().format(formatter);
            String codAlm = filtro.getCodAlm() != null ? filtro.getCodAlm() : "";
            
            logger.info("Ejecutando SP_CotizacionesPendientes_Atencion_SinSTK con parámetros: {} {} {}", 
                    fechaIni, fechaFin, codAlm);
            
            return jdbcTemplate.query(
                    "{call SP_CotizacionesPendientes_Atencion_SinSTK2(?, ?, ?)}",
                    new Object[]{fechaIni, fechaFin, codAlm},
                    new RowMapper<CotizacionPendiente>() {
                        @Override
                        public CotizacionPendiente mapRow(ResultSet rs, int rowNum) throws SQLException {
                            try {
                                if (rowNum == 0) {
                                    int colCount = rs.getMetaData().getColumnCount();
                                    StringBuilder colNames = new StringBuilder("Columnas disponibles: ");
                                    for (int i = 1; i <= colCount; i++) {
                                        colNames.append(rs.getMetaData().getColumnName(i)).append(", ");
                                    }
                                    logger.info(colNames.toString());
                                }
                                
                                CotizacionPendiente cotizacion = new CotizacionPendiente();
                                
                                cotizacion.setCodi(rs.getString("codi"));
                                cotizacion.setCodf(rs.getString("codf"));
                                cotizacion.setMarca(rs.getString("Marca"));
                                cotizacion.setStock(rs.getBigDecimal("Stock"));
                                cotizacion.setStockFechaCotizacion(rs.getBigDecimal("StockFechaCotizacion"));
                                cotizacion.setCantidadCotizacion(rs.getBigDecimal("CantidadCotización"));
                                cotizacion.setMoneda(rs.getString("Moneda"));
                                cotizacion.setPrecioDolares(rs.getBigDecimal("PrecioDolares"));
                                cotizacion.setCodCliente(rs.getString("CodCliente"));
                                cotizacion.setRznCliente(rs.getString("RznCliente"));
                                cotizacion.setNroCotizacion(rs.getString("NroCotizacion"));
                                
                                Timestamp fechaTimestamp = rs.getTimestamp("Fecha");
                                if (fechaTimestamp != null) {
                                    LocalDateTime dateTime = fechaTimestamp.toLocalDateTime();
                                    cotizacion.setFecha(dateTime.toLocalDate());
                                    
                                    if (rowNum < 5) {
                                        logger.debug("Fila {}: Fecha de BD (timestamp): {}, convertida a: {}", 
                                                rowNum, fechaTimestamp, dateTime.toLocalDate());
                                    }
                                } else {
                                    java.sql.Date fechaDate = rs.getDate("Fecha");
                                    if (fechaDate != null) {
                                        LocalDate fecha = fechaDate.toLocalDate();
                                        cotizacion.setFecha(fecha);
                                        
                                        if (rowNum < 5) {
                                            logger.debug("Fila {}: Fecha de BD (date): {}, convertida a: {}", 
                                                    rowNum, fechaDate, fecha);
                                        }
                                    }
                                }
                                
                                cotizacion.setVendedor(rs.getString("Vendedor"));
                                cotizacion.setOrden(rs.getString("Orden"));
                                cotizacion.setTipoOC(rs.getString("TipoOC"));
                                cotizacion.setProveedor(rs.getString("Proveedor"));
                                cotizacion.setPreOCDolar(rs.getBigDecimal("PreOCDolar"));
                                cotizacion.setNotaIngreso(rs.getString("NotaIngreso"));
                                
                                cotizacion.setFechaIng(rs.getString("FechaIng"));
                                
                                cotizacion.setCpu(rs.getBigDecimal("CPU"));
                                
                                return cotizacion;
                            } catch (SQLException e) {
                                logger.error("Error al mapear la fila {}: {}", rowNum, e.getMessage());
                                throw e;
                            }
                        }
                    });
                    
        } catch (DataAccessException e) {
            logger.error("Error al ejecutar SP_CotizacionesPendientes_Atencion_SinSTK: {}", e.getMessage(), e);
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error general: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}