package com.sudamericana.impoexcel.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.sudamericana.impoexcel.model.BancoCuenta;
import com.sudamericana.impoexcel.model.BancoEntidad;
import com.sudamericana.impoexcel.model.BancoEstado;
import com.sudamericana.impoexcel.model.BancoRegistro;
import com.sudamericana.impoexcel.model.ConciliacionDetalle;

@Repository
public interface ConciliacionBancariaRepository {
    List<BancoEntidad> obtenerBancos();
    List<BancoCuenta> obtenerCuentasPorEntidad(String entidad);
    BancoEstado obtenerEstadoConciliacion(String aniomes, String cuenta);
    List<BancoRegistro> obtenerRegistrosBanco(String aniomes, String cuenta);
    void insertarRegistroBanco(BancoRegistro registro);
    List<ConciliacionDetalle> obtenerEstadoCuentaBanco(String cuenta, String periodo);
    void actualizarEstadoConciliacion(String cuenta, String periodo, Integer item, Integer estadoRegistro, Integer itemConciliacion);
}