package com.sudamericana.impoexcel.model;


public class Almacen {
    private String codAlm;
    private String nomAlm;

    public Almacen() {
    }
    
    public Almacen(String codAlm, String nomAlm) {
        this.codAlm = codAlm;
        this.nomAlm = nomAlm;
    }

    public String getCodAlm() {
        return codAlm;
    }

    public void setCodAlm(String codAlm) {
        this.codAlm = codAlm;
    }

    public String getNomAlm() {
        return nomAlm;
    }

    public void setNomAlm(String nomAlm) {
        this.nomAlm = nomAlm;
    }

    
}