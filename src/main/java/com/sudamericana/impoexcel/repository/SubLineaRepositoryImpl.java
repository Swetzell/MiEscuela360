package com.sudamericana.impoexcel.repository;

import com.sudamericana.impoexcel.model.SubLinea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SubLineaRepositoryImpl implements SubLineaRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<SubLinea> subLineaRowMapper = (rs, rowNum) -> {
        SubLinea subLinea = new SubLinea();
        subLinea.setSublinea(rs.getString("SLINEA"));
        subLinea.setDescripcion(rs.getString("DESSLINEA"));
        return subLinea;
    };

    @Override
    public List<SubLinea> listarSubLineas() {
        try {
            return jdbcTemplate.query(
                "EXEC [PROD].[CON_LINEA] @LITIPSQL = ?, @LIENTIDA = ?, @LSLINEAS = ?, @LSPARAM1 = ?, @LSPARAM2 = ?",
                subLineaRowMapper,
                3, // Para el combo de sublineas
                1, // Entidad por defecto
                "", // LSLINEAS (se filtrará cuando se seleccione una línea)
                "",
                ""
            );
        } catch (DataAccessException e) {
            System.err.println("Error al listar sublíneas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<SubLinea> buscarSubLineas(String linea, String descripcion) {
        try {
            return jdbcTemplate.query(
                "EXEC [PROD].[CON_LINEA] @LITIPSQL = ?, @LIENTIDA = ?, @LSLINEAS = ?, @LSPARAM1 = ?, @LSPARAM2 = ?",
                subLineaRowMapper,
                3, // Para el combo de sublineas
                1, // Entidad por defecto
                linea, // LSLINEAS (código de línea seleccionada)
                descripcion,
                ""
            );
        } catch (DataAccessException e) {
            System.err.println("Error al buscar sublíneas: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
