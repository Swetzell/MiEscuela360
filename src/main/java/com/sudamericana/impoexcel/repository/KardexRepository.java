package com.sudamericana.impoexcel.repository;

import com.sudamericana.impoexcel.model.KardexDetalle;
import com.sudamericana.impoexcel.model.Producto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class KardexRepository {
    
    private static final Logger log = LoggerFactory.getLogger(KardexRepository.class);
    private static final int PAGE_SIZE = 30;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private SimpleJdbcCall consultarKardexProc;

    @PostConstruct
    public void init() {
        consultarKardexProc = new SimpleJdbcCall(jdbcTemplate)
            .withSchemaName("ALMA")
            .withProcedureName("CON_KARDEX")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("LITIPSQL", Types.SMALLINT),
                new SqlParameter("LIENTIDA", Types.SMALLINT),
                new SqlParameter("LSALMACE", Types.VARCHAR),
                new SqlParameter("LSPARAM1", Types.VARCHAR),
                new SqlParameter("LSPARAM2", Types.VARCHAR),
                new SqlParameter("LSPARAM3", Types.VARCHAR),
                new SqlParameter("LSPARAM4", Types.VARCHAR),
                new SqlParameter("LSPARAM5", Types.VARCHAR)
            )
            .returningResultSet("productos", (rs, rowNum) -> {
                Producto producto = new Producto();
                producto.setCodNava(rs.getString(1));         
                producto.setUnidadMedida(rs.getString(2));    
                producto.setProducto(rs.getString(3));        
                producto.setMarca(rs.getString(4));           
                producto.setDescripcion(rs.getString(5));     
                producto.setStockFisico(rs.getBigDecimal(6)); 
                producto.setStockTransi(rs.getBigDecimal(7)); 
                producto.setStockReserP(rs.getBigDecimal(8)); 
                producto.setStockConsig(rs.getBigDecimal(9)); 
                producto.setStockReserF(rs.getBigDecimal(10));
                producto.setDesLinea(rs.getString(32));     
                producto.setDesSubLinea(rs.getString(33));  
                producto.setDesGrupo(rs.getString(34));     
                producto.setStockDispon(rs.getBigDecimal(35));
                return producto;
            });
    }

    public List<Producto> buscarProductos(String almacen, String codigo, String descripcion, String linea, String marca, int page) {
        try {
            log.info("=== Iniciando búsqueda de productos === Página: {}", page);
            
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("LITIPSQL", (short)1)
                    .addValue("LIENTIDA", (short)1)
                    .addValue("LSALMACE", almacen != null ? almacen : "")
                    .addValue("LSPARAM1", codigo != null ? codigo : "")
                    .addValue("LSPARAM2", descripcion != null ? descripcion : "")
                    .addValue("LSPARAM3", linea != null ? linea : "")
                    .addValue("LSPARAM4", marca != null ? marca : "")
                    .addValue("LSPARAM5", "")
                    .addValue("PAGESIZE", PAGE_SIZE)
                    .addValue("PAGENUM", page);

            Map<String, Object> result = consultarKardexProc.execute(params);
            
            if (result == null) {
                log.warn("El stored procedure retornó null");
                return new ArrayList<>();
            }

            @SuppressWarnings("unchecked")
            List<Producto> productos = (List<Producto>) result.get("productos");
            
            if (productos == null) {
                log.warn("La lista de productos es null");
                return new ArrayList<>();
            }

            log.info("Búsqueda completada. Productos encontrados en página {}: {}", page, productos.size());
            return productos;

        } catch (Exception e) {
            log.error("Error en buscarProductos: ", e);
            throw new RuntimeException("Error al buscar productos", e);
        }
    }


    public List<KardexDetalle> buscarDetalleKardex(String codigoProducto, String fechaDesde, String fechaHasta) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("LITIPSQL", (short)2)
                    .addValue("LIENTIDA", (short)1)
                    .addValue("LSALMACE", "%")
                    .addValue("LSPARAM1", codigoProducto)
                    .addValue("LSPARAM2", fechaDesde != null ? fechaDesde : "")
                    .addValue("LSPARAM3", fechaHasta != null ? fechaHasta : "")
                    .addValue("LSPARAM4", "")
                    .addValue("LSPARAM5", "");

            Map<String, Object> result = consultarKardexProc.execute(params);
            return result.containsKey("detalles") ? 
                   (List<KardexDetalle>) result.get("detalles") : 
                   new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}