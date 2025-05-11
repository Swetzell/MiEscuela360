package com.sudamericana.impoexcel.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BancoRegistro {
    private String aniomes;
    private String cuenta;
    private Integer item;
    private LocalDate fecha;
    private String descripcion;
    private BigDecimal monto;
    private BigDecimal saldo;
    private String operacionNro;
    private String operacionHora;
    private String sucursal;
    private String referencia;
    private String campo1;
    private String campo2;
    private Integer estadoRegistro;
    private Integer itemConciliacion;
    
    // Campos adicionales para la visualización
    private BigDecimal montoNava;
    private BigDecimal montoDife;
    private String desEstado;
    private String estado;
    
    // Constructores
    public BancoRegistro() {
    }
    
    public BancoRegistro(String aniomes, String cuenta, Integer item, LocalDate fecha, String descripcion,
                       BigDecimal monto, BigDecimal saldo, String operacionNro, String operacionHora,
                       String sucursal, String referencia, String campo1, String campo2, Integer estadoRegistro,
                       Integer itemConciliacion, BigDecimal montoNava, BigDecimal montoDife, String desEstado, String estado) {
        this.aniomes = aniomes;
        this.cuenta = cuenta;
        this.item = item;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.monto = monto;
        this.saldo = saldo;
        this.operacionNro = operacionNro;
        this.operacionHora = operacionHora;
        this.sucursal = sucursal;
        this.referencia = referencia;
        this.campo1 = campo1;
        this.campo2 = campo2;
        this.estadoRegistro = estadoRegistro;
        this.itemConciliacion = itemConciliacion;
        this.montoNava = montoNava;
        this.montoDife = montoDife;
        this.desEstado = desEstado;
        this.estado = estado;
    }
    
    // Getters y Setters
    public String getAniomes() {
        return aniomes;
    }
    
    public void setAniomes(String aniomes) {
        this.aniomes = aniomes;
    }
    
    public String getCuenta() {
        return cuenta;
    }
    
    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }
    
    public Integer getItem() {
        return item;
    }
    
    public void setItem(Integer item) {
        this.item = item;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public BigDecimal getMonto() {
        return monto;
    }
    
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
    
    public BigDecimal getSaldo() {
        return saldo;
    }
    
    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
    
    public String getOperacionNro() {
        return operacionNro;
    }
    
    public void setOperacionNro(String operacionNro) {
        this.operacionNro = operacionNro;
    }
    
    public String getOperacionHora() {
        return operacionHora;
    }
    
    public void setOperacionHora(String operacionHora) {
        this.operacionHora = operacionHora;
    }
    
    public String getSucursal() {
        return sucursal;
    }
    
    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }
    
    public String getReferencia() {
        return referencia;
    }
    
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    
    public String getCampo1() {
        return campo1;
    }
    
    public void setCampo1(String campo1) {
        this.campo1 = campo1;
    }
    
    public String getCampo2() {
        return campo2;
    }
    
    public void setCampo2(String campo2) {
        this.campo2 = campo2;
    }
    
    public Integer getEstadoRegistro() {
        return estadoRegistro;
    }
    
    public void setEstadoRegistro(Integer estadoRegistro) {
        this.estadoRegistro = estadoRegistro;
    }
    
    public Integer getItemConciliacion() {
        return itemConciliacion;
    }
    
    public void setItemConciliacion(Integer itemConciliacion) {
        this.itemConciliacion = itemConciliacion;
    }
    
    public BigDecimal getMontoNava() {
        return montoNava;
    }
    
    public void setMontoNava(BigDecimal montoNava) {
        this.montoNava = montoNava;
    }
    
    public BigDecimal getMontoDife() {
        return montoDife;
    }
    
    public void setMontoDife(BigDecimal montoDife) {
        this.montoDife = montoDife;
    }
    
    public String getDesEstado() {
        return desEstado;
    }
    
    public void setDesEstado(String desEstado) {
        this.desEstado = desEstado;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return "BancoRegistro{" +
                "aniomes='" + aniomes + '\'' +
                ", cuenta='" + cuenta + '\'' +
                ", item=" + item +
                ", fecha=" + fecha +
                ", descripcion='" + descripcion + '\'' +
                ", monto=" + monto +
                ", saldo=" + saldo +
                ", operacionNro='" + operacionNro + '\'' +
                ", operacionHora='" + operacionHora + '\'' +
                ", sucursal='" + sucursal + '\'' +
                ", referencia='" + referencia + '\'' +
                ", campo1='" + campo1 + '\'' +
                ", campo2='" + campo2 + '\'' +
                ", estadoRegistro=" + estadoRegistro +
                ", itemConciliacion=" + itemConciliacion +
                ", montoNava=" + montoNava +
                ", montoDife=" + montoDife +
                ", desEstado='" + desEstado + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}