package com.sudamericana.impoexcel.model;


import java.math.BigDecimal;

public class ReposicionProducto {
    private String clase;
    private String codi;
    private String codf;
    private String descripcion;
    private String marca;
    private BigDecimal cantidadVendida;
    private Integer mesesVentas;
    private Integer nroVentas;
    private BigDecimal porcentajeMes;
    private BigDecimal porcentajeDia;
    private BigDecimal stockTotal;
    private BigDecimal factorSeguridad;
    private BigDecimal cantidadReponer;
    private String estado;
    private String fechaUltimaVenta;
    private BigDecimal ultimoPrecioCompra;
    private String ultimaOrdenCompra;
    private String fechaIngresoAlmacen;
    private String proveedor;
    private String glosa;
    private BigDecimal cantVentOS;
    
    // Stocks por almacén
    private BigDecimal stockLince;
    private BigDecimal stockArequipa;
    private BigDecimal stockSanLuis;
    private BigDecimal stockTrujillo;
    private BigDecimal stockCerroColorado;
    private BigDecimal stockLosOlivos;
    private BigDecimal stockChiclayo;
    private BigDecimal stockMercaderiaObsoletaSanLuis;
    private BigDecimal stockTomocorp1;
    private BigDecimal stockObsoletaBajaMercaderia;
    private BigDecimal stockMercaderiaObsoletaArequipa;
    private BigDecimal stockTransito;
    private String fechaTransferencia;
    private BigDecimal stockIni;
    private BigDecimal otroAlm;
    
    public ReposicionProducto() {
    }
    public String getClase() {
        return clase;
    }
    public void setClase(String clase) {
        this.clase = clase;
    }
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
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getMarca() {
        return marca;
    }
    public void setMarca(String marca) {
        this.marca = marca;
    }
    public BigDecimal getCantidadVendida() {
        return cantidadVendida;
    }
    public void setCantidadVendida(BigDecimal cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }
    public Integer getMesesVentas() {
        return mesesVentas;
    }
    public void setMesesVentas(Integer mesesVentas) {
        this.mesesVentas = mesesVentas;
    }
    public Integer getNroVentas() {
        return nroVentas;
    }
    public void setNroVentas(Integer nroVentas) {
        this.nroVentas = nroVentas;
    }
    public BigDecimal getPorcentajeMes() {
        return porcentajeMes;
    }
    public void setPorcentajeMes(BigDecimal porcentajeMes) {
        this.porcentajeMes = porcentajeMes;
    }
    public BigDecimal getPorcentajeDia() {
        return porcentajeDia;
    }
    public void setPorcentajeDia(BigDecimal porcentajeDia) {
        this.porcentajeDia = porcentajeDia;
    }
    public BigDecimal getStockTotal() {
        return stockTotal;
    }
    public void setStockTotal(BigDecimal stockTotal) {
        this.stockTotal = stockTotal;
    }
    public BigDecimal getFactorSeguridad() {
        return factorSeguridad;
    }
    public void setFactorSeguridad(BigDecimal factorSeguridad) {
        this.factorSeguridad = factorSeguridad;
    }
    public BigDecimal getCantidadReponer() {
        return cantidadReponer;
    }
    public void setCantidadReponer(BigDecimal cantidadReponer) {
        this.cantidadReponer = cantidadReponer;
    }
    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }
    public String getFechaUltimaVenta() {
        return fechaUltimaVenta;
    }
    public void setFechaUltimaVenta(String fechaUltimaVenta) {
        this.fechaUltimaVenta = fechaUltimaVenta;
    }
    public BigDecimal getUltimoPrecioCompra() {
        return ultimoPrecioCompra;
    }
    public void setUltimoPrecioCompra(BigDecimal ultimoPrecioCompra) {
        this.ultimoPrecioCompra = ultimoPrecioCompra;
    }
    public String getUltimaOrdenCompra() {
        return ultimaOrdenCompra;
    }
    public void setUltimaOrdenCompra(String ultimaOrdenCompra) {
        this.ultimaOrdenCompra = ultimaOrdenCompra;
    }
    public String getFechaIngresoAlmacen() {
        return fechaIngresoAlmacen;
    }
    public void setFechaIngresoAlmacen(String fechaIngresoAlmacen) {
        this.fechaIngresoAlmacen = fechaIngresoAlmacen;
    }
    public String getProveedor() {
        return proveedor;
    }
    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
    public String getGlosa() {
        return glosa;
    }
    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }
    public BigDecimal getStockLince() {
        return stockLince;
    }
    public void setStockLince(BigDecimal stockLince) {
        this.stockLince = stockLince;
    }
    public BigDecimal getStockArequipa() {
        return stockArequipa;
    }
    public void setStockArequipa(BigDecimal stockArequipa) {
        this.stockArequipa = stockArequipa;
    }
    public BigDecimal getStockSanLuis() {
        return stockSanLuis;
    }
    public void setStockSanLuis(BigDecimal stockSanLuis) {
        this.stockSanLuis = stockSanLuis;
    }
    public BigDecimal getStockTrujillo() {
        return stockTrujillo;
    }
    public void setStockTrujillo(BigDecimal stockTrujillo) {
        this.stockTrujillo = stockTrujillo;
    }
    public BigDecimal getStockCerroColorado() {
        return stockCerroColorado;
    }
    public void setStockCerroColorado(BigDecimal stockCerroColorado) {
        this.stockCerroColorado = stockCerroColorado;
    }
    public BigDecimal getStockLosOlivos() {
        return stockLosOlivos;
    }
    public void setStockLosOlivos(BigDecimal stockLosOlivos) {
        this.stockLosOlivos = stockLosOlivos;
    }
    public BigDecimal getStockChiclayo() {
        return stockChiclayo;
    }
    public void setStockChiclayo(BigDecimal stockChiclayo) {
        this.stockChiclayo = stockChiclayo;
    }
    public BigDecimal getStockMercaderiaObsoletaSanLuis() {
        return stockMercaderiaObsoletaSanLuis;
    }
    public void setStockMercaderiaObsoletaSanLuis(BigDecimal stockMercaderiaObsoletaSanLuis) {
        this.stockMercaderiaObsoletaSanLuis = stockMercaderiaObsoletaSanLuis;
    }
    public BigDecimal getStockTomocorp1() {
        return stockTomocorp1;
    }
    public void setStockTomocorp1(BigDecimal stockTomocorp1) {
        this.stockTomocorp1 = stockTomocorp1;
    }
    public BigDecimal getStockObsoletaBajaMercaderia() {
        return stockObsoletaBajaMercaderia;
    }
    public void setStockObsoletaBajaMercaderia(BigDecimal stockObsoletaBajaMercaderia) {
        this.stockObsoletaBajaMercaderia = stockObsoletaBajaMercaderia;
    }
    public BigDecimal getStockMercaderiaObsoletaArequipa() {
        return stockMercaderiaObsoletaArequipa;
    }
    public void setStockMercaderiaObsoletaArequipa(BigDecimal stockMercaderiaObsoletaArequipa) {
        this.stockMercaderiaObsoletaArequipa = stockMercaderiaObsoletaArequipa;
    }
    public BigDecimal getStockTransito() {
        return stockTransito;
    }
    public void setStockTransito(BigDecimal stockTransito) {
        this.stockTransito = stockTransito;
    }
    public String getFechaTransferencia() {
        return fechaTransferencia;
    }
    public void setFechaTransferencia(String fechaTransferencia) {
        this.fechaTransferencia = fechaTransferencia;
    }
    public BigDecimal getCantVentOS() {
        return cantVentOS;
    }
    public void setCantVentOS(BigDecimal cantVentOS) {
        this.cantVentOS = cantVentOS;
    }
    public BigDecimal getStockIni() {
        return stockIni;
    }
    public void setStockIni(BigDecimal stockIni) {
        this.stockIni = stockIni;
    }
    public BigDecimal getOtroAlm() {
        return otroAlm;
    }
    public void setOtroAlm(BigDecimal otroAlm) {
        this.otroAlm = otroAlm;
    }
  
    
}
