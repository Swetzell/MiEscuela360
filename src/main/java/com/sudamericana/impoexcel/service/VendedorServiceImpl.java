package com.sudamericana.impoexcel.service;

import com.sudamericana.impoexcel.model.Vendedor;
import com.sudamericana.impoexcel.repository.VendedorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Service
public class VendedorServiceImpl implements VendedorRepository {
    private static final Logger logger = LoggerFactory.getLogger(VendedorServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public VendedorServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Cacheable("vendedores")
    public List<Vendedor> listarTodos() {
        logger.info("Obteniendo lista de vendedores desde la base de datos");
        String sql = "{CALL PERS.CON_PERSONAL(?, ?, ?, ?, ?)}";
        
        try {
            return jdbcTemplate.query(
                connection -> {
                    try {
                        connection.setNetworkTimeout(null, 10000); 
                        var callableStatement = connection.prepareCall(sql);
                        callableStatement.setQueryTimeout(10);
                        callableStatement.setInt(1, 6);
                        callableStatement.setInt(2, 0);
                        callableStatement.setString(3, "");
                        callableStatement.setString(4, "");
                        callableStatement.setString(5, "");
                        return callableStatement;
                    } catch (SQLException e) {
                        logger.error("Error preparing call: {}", e.getMessage(), e);
                        throw e;
                    }
                },
                (rs, rowNum) -> {
                    try {
                        String id = rs.getString("CODIGO");
                        String nombre = rs.getString("NOMBRES");
                        logger.debug("Mapped vendor: {} - {}", id, nombre);
                        return new Vendedor(id, nombre);
                    } catch (SQLException e) {
                        logger.error("Error al mapear vendedor en fila {}: {}", rowNum, e.getMessage());
                        throw e;
                    }
                }
            );
        } catch (DataAccessException e) {
            logger.error("Error al obtener vendedores: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}