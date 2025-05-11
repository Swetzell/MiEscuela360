package com.sudamericana.impoexcel.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.sudamericana.impoexcel.model.BancoCuenta;
import com.sudamericana.impoexcel.model.BancoEntidad;
import com.sudamericana.impoexcel.model.BancoEstado;
import com.sudamericana.impoexcel.model.BancoRegistro;
import com.sudamericana.impoexcel.model.ConciliacionDetalle;

@Repository
public class ConciliacionBancariaRepositoryImpl implements ConciliacionBancariaRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public List<BancoEntidad> obtenerBancos() {
        List<BancoEntidad> bancos = new ArrayList<>();
        bancos.add(new BancoEntidad("1", "SUDAMERICANA DE RODAMIENTOS"));
        bancos.add(new BancoEntidad("2", "SUDAMERICANA LATIN AMERICA S.A.C"));
        return bancos;
    }
    
    @Override
    public List<BancoCuenta> obtenerCuentasPorEntidad(String entidad) {
        String sql = "EXEC [FINA].[CON_BANCO] @LITIPSQL = 1, @LSPARAM1 = ?";
        return jdbcTemplate.query(sql, new Object[]{entidad}, (rs, rowNum) -> {
            BancoCuenta cuenta = new BancoCuenta();
            cuenta.setCodbco(rs.getString("codbco"));
            cuenta.setNombco(rs.getString("nombco"));
            cuenta.setNrocta(rs.getString("nrocta"));
            cuenta.setMone(rs.getString("mone"));
            cuenta.setDescrip(rs.getString("descrip"));
            return cuenta;
        });
    }
    
    @Override
    public BancoEstado obtenerEstadoConciliacion(String aniomes, String cuenta) {
        BancoEstado estado = new BancoEstado(0, "Sin conciliar");
        
        String sql = "SELECT COUNT(*) FROM [FINA].[REGISTRO_BANCO] WHERE ANIOMES = ? AND CUENTA = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{aniomes, cuenta}, Integer.class);
        
        if (count != null && count > 0) {
            estado.setEstado(1);
            estado.setDescrip("Con registros");
        }
        
        return estado;
    }
    
    @Override
    public List<BancoRegistro> obtenerRegistrosBanco(String aniomes, String cuenta) {
        String sql = "SELECT * FROM [FINA].[REGISTRO_BANCO] WHERE ANIOMES = ? AND CUENTA = ? ORDER BY FECHA, ITEM";
        
        return jdbcTemplate.query(sql, new Object[]{aniomes, cuenta}, new RowMapper<BancoRegistro>() {
            @Override
            public BancoRegistro mapRow(ResultSet rs, int rowNum) throws SQLException {
                BancoRegistro registro = new BancoRegistro();
                registro.setAniomes(rs.getString("ANIOMES"));
                registro.setCuenta(rs.getString("CUENTA"));
                registro.setItem(rs.getInt("ITEM"));
                registro.setFecha(rs.getDate("FECHA").toLocalDate());
                registro.setDescripcion(rs.getString("DESCRIPCION"));
                registro.setMonto(rs.getBigDecimal("MONTO"));
                registro.setSaldo(rs.getBigDecimal("SALDO"));
                registro.setOperacionNro(rs.getString("OPERACION_NRO"));
                registro.setOperacionHora(rs.getString("OPERACION_HORA"));
                registro.setSucursal(rs.getString("SUCURSAL"));
                registro.setReferencia(rs.getString("REFERENCIA"));
                registro.setCampo1(rs.getString("CAMPO1"));
                registro.setCampo2(rs.getString("CAMPO2"));
                registro.setEstadoRegistro(rs.getInt("ESTADO_REGISTRO"));
                registro.setItemConciliacion(rs.getInt("ITEM_CONCILIACION"));
                
                // Campos calculados para la vista
                registro.setMontoNava(BigDecimal.ZERO);
                registro.setMontoDife(registro.getMonto());
                
                // Estado para visualización
                String estadoTexto = "";
                String colorEstado = "5"; 
                
                switch (registro.getEstadoRegistro()) {
                    case 1: 
                        estadoTexto = "Pendiente";
                        colorEstado = "1"; // Rojo
                        break;
                    case 2: 
                        estadoTexto = "Conciliado Parcial";
                        colorEstado = "2"; // Azul
                        break;
                    case 3: 
                        estadoTexto = "Conciliado Total";
                        colorEstado = "3"; // Verde
                        break;
                    case 4: 
                        estadoTexto = "Comisión ITF";
                        colorEstado = "4"; // Morado
                        break;
                    default: 
                        estadoTexto = "Desconocido";
                }
                
                registro.setDesEstado(estadoTexto);
                registro.setEstado(colorEstado);
                
                return registro;
            }
        });
    }
    
    @Override
    public void insertarRegistroBanco(BancoRegistro registro) {
        // Formatear fecha para ANIOMES (MMYYYY)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMyyyy");
        String aniomes = registro.getFecha().format(formatter);
        
        String sql = "DECLARE @OUTPUT VARCHAR(100)\n" +
                "EXEC [FINA].[SQL_REGISTRO_BANCO] " +
                "@LITIPSQL = 1, " +
                "@LSANIMES = ?, " +
                "@LSCUENTA = ?, " +
                "@LINROITM = 0, " +
                "@LDFECHAS = ?, " +
                "@LSDESCRI = ?, " +
                "@LNMONTOS = ?, " +
                "@LNSALDOS = ?, " +
                "@LSNROOPE = ?, " +
                "@LSHOROPE = ?, " +
                "@LSSUCURS = ?, " +
                "@LSREFERE = ?, " +
                "@LSCAMPO1 = ?, " +
                "@LSCAMPO2 = ?, " +
                "@LINUMRES = @OUTPUT OUTPUT";
        
        jdbcTemplate.update(sql, 
                aniomes,
                registro.getCuenta(),
                java.sql.Date.valueOf(registro.getFecha()),
                registro.getDescripcion(),
                registro.getMonto(),
                registro.getSaldo(),
                registro.getOperacionNro(),
                registro.getOperacionHora(),
                registro.getSucursal(),
                registro.getReferencia(),
                registro.getCampo1(),
                registro.getCampo2());
    }
    
    @Override
    public List<ConciliacionDetalle> obtenerEstadoCuentaBanco(String cuenta, String periodo) {
        try {
            String cuentaSanitizada = cuenta.replaceAll("[^0-9]", "");
            
            if (cuentaSanitizada.isEmpty() && cuenta.contains("-")) {
                System.out.println("ADVERTENCIA: La cuenta " + cuenta + " contiene solo caracteres no numéricos. " +
                                   "Esto puede causar errores de conversión en el procedimiento almacenado.");
                String[] partes = cuenta.split("-");
                for (String parte : partes) {
                    if (parte.matches("\\d+")) {
                        cuentaSanitizada = parte;
                        break;
                    }
                }
                if (cuentaSanitizada.isEmpty()) {
                    cuentaSanitizada = "0";
                }
            }
            
            // Asegurarse de que el periodo esté en formato numérico
            String periodoSanitizado = periodo.replaceAll("[^0-9]", "");
            
            // Log para depuración
            System.out.println("Ejecutando SP con cuenta: " + cuentaSanitizada + ", periodo: " + periodoSanitizado);
            
            String sql = "EXEC [dbo].[Sp_estado_cuenta_bco_excel] @cuentaBanco = ?, @periodo = ?";
            
            return jdbcTemplate.query(sql, new Object[]{cuentaSanitizada, periodoSanitizado}, (rs, rowNum) -> {
                ConciliacionDetalle detalle = new ConciliacionDetalle();
                
                // El resto del código permanece igual
                detalle.setCuenta(rs.getString("Cuenta"));
                detalle.setFecha(rs.getString("Fecha"));
                detalle.setDescripcion(rs.getString("Descripción"));
                detalle.setMonto(rs.getBigDecimal("Monto"));
                detalle.setNroOperacion(rs.getString("NroOperación"));
                detalle.setFechaPlanilla(rs.getString("Fechapla"));
                detalle.setCdocu(rs.getString("cdocu"));
                detalle.setNdocu(rs.getString("ndocu"));
                detalle.setNplan(rs.getString("nplan"));
                detalle.setMontoPlanilla(rs.getBigDecimal("montopla"));
                detalle.setNomDocRef(rs.getString("nomdocref"));
                detalle.setNrefe(rs.getString("nrefe"));
                detalle.setMontoDoc(rs.getBigDecimal("montodoc"));
                detalle.setGlosa(rs.getString("glosa"));
                detalle.setCodcli(rs.getString("codcli"));
                detalle.setNomcli(rs.getString("nomcli"));
                detalle.setNomusu(rs.getString("nomusu"));
                detalle.setFecreg(rs.getString("fecreg"));
                
                if (detalle.isConciliado()) {
                    detalle.setEstado("3"); // Conciliado Total
                } else if (detalle.isConciliadoParcial()) {
                    detalle.setEstado("2"); // Conciliado Parcial
                } else {
                    detalle.setEstado("1"); // Pendiente
                }
                
                return detalle;
            });
        } catch (Exception e) {
            // Registrar el error para diagnóstico
            System.err.println("Error al ejecutar SP_estado_cuenta_bco_excel: " + e.getMessage());
            e.printStackTrace();
            
            return new ArrayList<>();
        }
    }
    
    @Override
    public void actualizarEstadoConciliacion(String cuenta, String periodo, Integer item, Integer estadoRegistro, Integer itemConciliacion) {
        String sql = "UPDATE [FINA].[REGISTRO_BANCO] SET ESTADO_REGISTRO = ?, ITEM_CONCILIACION = ? " +
                     "WHERE ANIOMES = ? AND CUENTA = ? AND ITEM = ?";
        
        jdbcTemplate.update(sql, estadoRegistro, itemConciliacion, periodo, cuenta, item);
    }
}