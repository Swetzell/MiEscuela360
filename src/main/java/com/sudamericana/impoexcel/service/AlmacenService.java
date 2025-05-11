package com.sudamericana.impoexcel.service;

import com.sudamericana.impoexcel.model.Almacen;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class AlmacenService {
    
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(AlmacenService.class);
    
    @Autowired
    public AlmacenService(@Qualifier("sqlServerDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public List<Almacen> listarAlmacenes() {
        try {
            logger.info("Obteniendo lista de almacenes");
            
            return jdbcTemplate.query(
                    "{call SP_ListaAlmacenes_sg}",
                    new RowMapper<Almacen>() {
                        @Override
                        public Almacen mapRow(ResultSet rs, int rowNum) throws SQLException {
                            Almacen almacen = new Almacen();
                            almacen.setCodAlm(rs.getString("codalm"));
                            almacen.setNomAlm(rs.getString("Nomalm"));
                            return almacen;
                        }
                    });
                    
        } catch (DataAccessException e) {
            logger.error("Error al obtener la lista de almacenes: {}", e.getMessage(), e);
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error general: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    public Almacen obtenerAlmacenPorCodigo(String codigoAlmacen) {
        try {
            List<Almacen> almacenes = listarAlmacenes();
            return almacenes.stream()
                    .filter(a -> a.getCodAlm().equals(codigoAlmacen))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error al obtener almacén por código: {}", e.getMessage(), e);
            return null;
        }
    }

    public String obtenerNombreAlmacen(String codAlm) {
        switch (codAlm) {
            case "01":
                return "LINCE";
            case "02":
                return "AREQUIPA";
            case "03":
                return "SAN LUIS";
            case "04":
                return "TRUJILLO";
            case "05":
                return "CERRO COLORADO";
            case "06":
                return "LOS OLIVOS";
            case "07":
                return "CHICLAYO";
            case "08":
                return "MERCADERÍA OBSOLETA SAN LUIS";
            case "09":
                return "TOMOCORP";
            case "10":
                return "TOMOCORP 1";
            case "11":
                return "OTROS";
            case "12":
                return "OBSOLETA BAJA MERCADERÍA";
            case "13":
                return "MERCADERÍA OBSOLETA AREQUIPA";
            default:
                return codAlm;
        }
    }
}