package com.sudamericana.impoexcel.model;

public class SubLinea {
    private String sublinea;
    private String descripcion;

    public SubLinea() {
    }

    public SubLinea(String sublinea, String descripcion) {
        this.sublinea = sublinea;
        this.descripcion = descripcion;
    }

    public String getSublinea() {
        return sublinea;
    }

    public void setSublinea(String sublinea) {
        this.sublinea = sublinea;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
