package com.sudamericana.impoexcel.repository;

import com.sudamericana.impoexcel.model.Grupo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GrupoRepositoryImpl implements GrupoRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Grupo> grupoRowMapper = (rs, rowNum) -> {
        Grupo grupo = new Grupo();
        grupo.setGrupo(rs.getString("GRUPO"));
        grupo.setDescripcion(rs.getString("DESGRUPO"));
        return grupo;
    };

    @Override
    public List<Grupo> listarGrupos() {
        try {
            return jdbcTemplate.query(
                "EXEC [PROD].[CON_LINEA] @LITIPSQL = ?, @LIENTIDA = ?, @LSLINEAS = ?, @LSPARAM1 = ?, @LSPARAM2 = ?",
                grupoRowMapper,
                4, // Para el combo de grupos
                1, // Entidad por defecto
                "", // LSLINEAS (se filtrará cuando se seleccione una línea)
                "", // LSPARAM1 (se filtrará cuando se seleccione una sublínea)
                ""
            );
        } catch (DataAccessException e) {
            System.err.println("Error al listar grupos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Grupo> buscarGrupos(String sublinea, String descripcion) {
        try {
            String[] parts = sublinea.split("-");
            String linea = parts.length > 0 ? parts[0] : "";
            String sublineaCode = parts.length > 1 ? parts[1] : "";
            
            return jdbcTemplate.query(
                "EXEC [PROD].[CON_LINEA] @LITIPSQL = ?, @LIENTIDA = ?, @LSLINEAS = ?, @LSPARAM1 = ?, @LSPARAM2 = ?",
                grupoRowMapper,
                4, // Para el combo de grupos
                1, // Entidad por defecto
                linea, // LSLINEAS (código de línea)
                sublineaCode, // LSPARAM1 (código de sublínea)
                descripcion
            );
        } catch (DataAccessException e) {
            System.err.println("Error al buscar grupos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}