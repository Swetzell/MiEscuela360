package com.sudamericana.impoexcel.model;

import java.math.BigDecimal;

public class KardexDetalle {
 private String fecha;
    private String documento;
    private String empresa;
    private BigDecimal entra;
    private BigDecimal sale;
    private BigDecimal precio;
    private String glosa;
    private String referencia;
    private String oc;
    private String dregistro;

    public KardexDetalle() {
    }

    public KardexDetalle(String fecha, String documento, String empresa, BigDecimal entra, BigDecimal sale, BigDecimal precio, String glosa, String referencia, String oc, String dregistro) {
        this.fecha = fecha;
        this.documento = documento;
        this.empresa = empresa;
        this.entra = entra;
        this.sale = sale;
        this.precio = precio;
        this.glosa = glosa;
        this.referencia = referencia;
        this.oc = oc;
        this.dregistro = dregistro;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public BigDecimal getEntra() {
        return entra;
    }

    public void setEntra(BigDecimal entra) {
        this.entra = entra;
    }

    public BigDecimal getSale() {
        return sale;
    }

    public void setSale(BigDecimal sale) {
        this.sale = sale;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getGlosa() {
        return glosa;
    }

    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getOc() {
        return oc;
    }

    public void setOc(String oc) {
        this.oc = oc;
    }

    public String getDregistro() {
        return dregistro;
    }

    public void setDregistro(String dregistro) {
        this.dregistro = dregistro;
    }

    
}
