package com.sudamericana.impoexcel.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.sudamericana.impoexcel.model.Marca;

@Service
public class MarcaService {

    private static final Logger logger = LoggerFactory.getLogger(MarcaService.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MarcaService(@Qualifier("sqlServerDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public List<Marca> listarTodas() {
        try {
            logger.info("Obteniendo lista de marcas usando SP_ListaMarcas_sg");
            
            return jdbcTemplate.query(
                    "{call SP_ListaMarcas_sg}",
                    new RowMapper<Marca>() {
                        @Override
                        public Marca mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new Marca(
                                rs.getString("codmar"),
                                rs.getString("Nommar")
                            );
                        }
                    });
                    
        } catch (DataAccessException e) {
            logger.error("Error al obtener la lista de marcas con SP_ListaMarcas_sg: {}", e.getMessage(), e);
            return listarMarcasAlternativo();
        }
    }
    
    private List<Marca> listarMarcasAlternativo() {
        try {
            logger.info("Intentando obtener marcas mediante consulta directa");
            
            String sql = "SELECT codmar, ltrim(rtrim(Nommar)) as Nommar FROM BdNava01.dbo.tbl01mar WHERE codmar <> '0000' ORDER BY Nommar";
            
            List<Marca> marcas = jdbcTemplate.query(
                sql,
                new RowMapper<Marca>() {
                    @Override
                    public Marca mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Marca(
                            rs.getString("codmar"),
                            rs.getString("Nommar")
                        );
                    }
                });
            
            logger.info("Marcas recuperadas mediante consulta directa: {}", marcas.size());
            
            // Aseguramos que siempre tenga la opción "Todas las marcas" al inicio
            if (!marcas.isEmpty()) {
                List<Marca> marcasConTodas = new ArrayList<>();
                marcasConTodas.add(new Marca("0000", "Todas las marcas"));
                marcasConTodas.addAll(marcas);
                return marcasConTodas;
            }
            
            return marcas;
            
        } catch (DataAccessException e) {
            logger.error("Error al obtener marcas mediante consulta directa: {}", e.getMessage(), e);
            
            // Si todo falla, devolvemos al menos la opción por defecto
            List<Marca> marcasDefault = new ArrayList<>();
            marcasDefault.add(new Marca("0000", "Todas las marcas"));
            return marcasDefault;
        }
    }
    
    public Marca obtenerMarcaPorCodigo(String codigoMarca) {
        try {
            logger.info("Buscando marca con código: {}", codigoMarca);
            
            if ("0000".equals(codigoMarca)) {
                return new Marca("0000", "Todas las marcas");
            }
            
            String sql = "SELECT codmar, ltrim(rtrim(Nommar)) as Nommar FROM BdNava01.dbo.tbl01mar WHERE codmar = ?";
            
            return jdbcTemplate.queryForObject(
                sql,
                new Object[] { codigoMarca },
                new RowMapper<Marca>() {
                    @Override
                    public Marca mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Marca(
                            rs.getString("codmar"),
                            rs.getString("Nommar")
                        );
                    }
                });
                
        } catch (DataAccessException e) {
            logger.error("Error al obtener marca con código {}: {}", codigoMarca, e.getMessage());
            return null;
        }
    }
}