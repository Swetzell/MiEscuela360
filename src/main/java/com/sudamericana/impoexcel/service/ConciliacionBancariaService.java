package com.sudamericana.impoexcel.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.sudamericana.impoexcel.model.BancoCuenta;
import com.sudamericana.impoexcel.model.BancoEntidad;
import com.sudamericana.impoexcel.model.BancoEstado;
import com.sudamericana.impoexcel.model.BancoRegistro;
import com.sudamericana.impoexcel.model.ConciliacionDetalle;

public interface ConciliacionBancariaService {
    List<BancoEntidad> obtenerEntidades();
    List<BancoCuenta> obtenerCuentasPorEntidad(String entidad);
    BancoEstado obtenerEstadoConciliacion(String aniomes, String cuenta);
    List<BancoRegistro> obtenerRegistrosBanco(String aniomes, String cuenta);
    boolean procesarArchivoExcel(MultipartFile file, String cuenta, String aniomes);
    List<ConciliacionDetalle> procesarConciliacionBancaria(String cuenta, String periodo);
    boolean actualizarEstadoConciliacion(String cuenta, String periodo, Integer item, Integer estadoRegistro, Integer itemConciliacion);
    List<Map<String, Object>> procesarConciliacionAutomatica(String cuentaBanco, String periodo);
    byte[] exportarConciliacionExcel(String cuentaBanco, String periodo);
}
