package com.sudamericana.impoexcel.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CotizacionPendiente {
    private String codi;
    private String codf;
    private String marca;
    private BigDecimal stock;
    private BigDecimal stockFechaCotizacion;
    private BigDecimal cantidadCotizacion;
    private String moneda;
    private BigDecimal precioDolares;
    private String codCliente;
    private String rznCliente;
    private String nroCotizacion;
    private LocalDate fecha;
    private String vendedor;
    private String orden;
    private String tipoOC;
    private String proveedor;
    private BigDecimal preOCDolar;
    private String notaIngreso;
    private String fechaIng;
    private BigDecimal cpu;
    public String getCodi() {
        return codi;
    }
    public void setCodi(String codi) {
        this.codi = codi;
    }
    public String getCodf() {
        return codf;
    }
    public void setCodf(String codf) {
        this.codf = codf;
    }
    public String getMarca() {
        return marca;
    }
    public void setMarca(String marca) {
        this.marca = marca;
    }
    public BigDecimal getStock() {
        return stock;
    }
    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }

    public BigDecimal getStockFechaCotizacion() {
        return stockFechaCotizacion;
    }
    
    public void setStockFechaCotizacion(BigDecimal stockFechaCotizacion) {
        this.stockFechaCotizacion = stockFechaCotizacion;
    }
    public BigDecimal getCantidadCotizacion() {
        return cantidadCotizacion;
    }
    public void setCantidadCotizacion(BigDecimal cantidadCotizacion) {
        this.cantidadCotizacion = cantidadCotizacion;
    }
    public String getMoneda() {
        return moneda;
    }
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
    public BigDecimal getPrecioDolares() {
        return precioDolares;
    }
    public void setPrecioDolares(BigDecimal precioDolares) {
        this.precioDolares = precioDolares;
    }
    public String getCodCliente() {
        return codCliente;
    }
    public void setCodCliente(String codCliente) {
        this.codCliente = codCliente;
    }
    public String getRznCliente() {
        return rznCliente;
    }
    public void setRznCliente(String rznCliente) {
        this.rznCliente = rznCliente;
    }
    public String getNroCotizacion() {
        return nroCotizacion;
    }
    public void setNroCotizacion(String nroCotizacion) {
        this.nroCotizacion = nroCotizacion;
    }
    public LocalDate getFecha() {
        return fecha;
    }
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    public String getVendedor() {
        return vendedor;
    }
    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }
    public String getOrden() {
        return orden;
    }
    public void setOrden(String orden) {
        this.orden = orden;
    }
    public String getTipoOC() {
        return tipoOC;
    }
    public void setTipoOC(String tipoOC) {
        this.tipoOC = tipoOC;
    }
    public String getProveedor() {
        return proveedor;
    }
    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
    public BigDecimal getPreOCDolar() {
        return preOCDolar;
    }
    public void setPreOCDolar(BigDecimal preOCDolar) {
        this.preOCDolar = preOCDolar;
    }
    public String getNotaIngreso() {
        return notaIngreso;
    }
    public void setNotaIngreso(String notaIngreso) {
        this.notaIngreso = notaIngreso;
    }
    public String getFechaIng() {
        return fechaIng;
    }
    public void setFechaIng(String fechaIng) {
        this.fechaIng = fechaIng;
    }
    public BigDecimal getCpu() {
        return cpu;
    }
    public void setCpu(BigDecimal cpu) {
        this.cpu = cpu;
    }

    
}
