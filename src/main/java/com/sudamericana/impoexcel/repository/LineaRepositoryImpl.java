package com.sudamericana.impoexcel.repository;


import com.sudamericana.impoexcel.model.Linea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LineaRepositoryImpl implements LineaRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Linea> lineaRowMapper = (rs, rowNum) -> {
        Linea linea = new Linea();
        linea.setLinea(rs.getString("LINEA"));
        linea.setDescripcion(rs.getString("DESLINEA"));
        return linea;
    };

    @Override
    public List<Linea> listarLineas() {
        try {
            return jdbcTemplate.query(
                "EXEC [PROD].[CON_LINEA] @LITIPSQL = ?, @LIENTIDA = ?, @LSLINEAS = ?, @LSPARAM1 = ?, @LSPARAM2 = ?",
                lineaRowMapper,
                2, // Para el combo de líneas
                1, // Entidad por defecto
                "",
                "",
                ""
            );
        } catch (DataAccessException e) {
            System.err.println("Error al listar líneas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Linea> buscarLineas(String linea, String descripcion) {
        try {
            return jdbcTemplate.query(
                "EXEC [PROD].[CON_LINEA] @LITIPSQL = ?, @LIENTIDA = ?, @LSLINEAS = ?, @LSPARAM1 = ?, @LSPARAM2 = ?",
                lineaRowMapper,
                2, // Para el combo de líneas
                1, // Entidad por defecto
                linea,
                descripcion,
                ""
            );
        } catch (DataAccessException e) {
            System.err.println("Error al buscar líneas: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}