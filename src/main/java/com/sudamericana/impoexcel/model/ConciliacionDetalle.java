package com.sudamericana.impoexcel.model;

import java.math.BigDecimal;

public class ConciliacionDetalle {
    private String cuenta;
    private String fecha;
    private String descripcion;
    private BigDecimal monto;
    private String nroOperacion;
    private String fechaPlanilla;
    private String cdocu;
    private String ndocu;
    private String nplan;
    private BigDecimal montoPlanilla;
    private String nomDocRef;
    private String nrefe;
    private BigDecimal montoDoc;
    private String glosa;
    private String codcli;
    private String nomcli;
    private String nomusu;
    private String fecreg;
    private Integer item;
    private String estado;

    // Constructor vacío
    public ConciliacionDetalle() {
    }

    // Getters y setters
    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
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

    public String getNroOperacion() {
        return nroOperacion;
    }

    public void setNroOperacion(String nroOperacion) {
        this.nroOperacion = nroOperacion;
    }

    public String getFechaPlanilla() {
        return fechaPlanilla;
    }

    public void setFechaPlanilla(String fechaPlanilla) {
        this.fechaPlanilla = fechaPlanilla;
    }

    public String getCdocu() {
        return cdocu;
    }

    public void setCdocu(String cdocu) {
        this.cdocu = cdocu;
    }

    public String getNdocu() {
        return ndocu;
    }

    public void setNdocu(String ndocu) {
        this.ndocu = ndocu;
    }

    public String getNplan() {
        return nplan;
    }

    public void setNplan(String nplan) {
        this.nplan = nplan;
    }

    public BigDecimal getMontoPlanilla() {
        return montoPlanilla;
    }

    public void setMontoPlanilla(BigDecimal montoPlanilla) {
        this.montoPlanilla = montoPlanilla;
    }

    public String getNomDocRef() {
        return nomDocRef;
    }

    public void setNomDocRef(String nomDocRef) {
        this.nomDocRef = nomDocRef;
    }

    public String getNrefe() {
        return nrefe;
    }

    public void setNrefe(String nrefe) {
        this.nrefe = nrefe;
    }

    public BigDecimal getMontoDoc() {
        return montoDoc;
    }

    public void setMontoDoc(BigDecimal montoDoc) {
        this.montoDoc = montoDoc;
    }

    public String getGlosa() {
        return glosa;
    }

    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }

    public String getCodcli() {
        return codcli;
    }

    public void setCodcli(String codcli) {
        this.codcli = codcli;
    }

    public String getNomcli() {
        return nomcli;
    }

    public void setNomcli(String nomcli) {
        this.nomcli = nomcli;
    }

    public String getNomusu() {
        return nomusu;
    }

    public void setNomusu(String nomusu) {
        this.nomusu = nomusu;
    }

    public String getFecreg() {
        return fecreg;
    }

    public void setFecreg(String fecreg) {
        this.fecreg = fecreg;
    }
    
    public Integer getItem() {
        return item;
    }

    public void setItem(Integer item) {
        this.item = item;
    }
    
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    // Método para determinar si un registro está conciliado
    public boolean isConciliado() {
        // Si tiene montoPlanilla y es igual al monto, está conciliado
        return montoPlanilla != null && monto != null && 
               montoPlanilla.compareTo(monto) == 0 && !nplan.isEmpty();
    }
    
    // Método para determinar si un registro está conciliado parcialmente
    public boolean isConciliadoParcial() {
        // Si tiene montoPlanilla pero es diferente al monto
        return montoPlanilla != null && monto != null && 
               montoPlanilla.compareTo(monto) != 0 && !nplan.isEmpty();
    }
}