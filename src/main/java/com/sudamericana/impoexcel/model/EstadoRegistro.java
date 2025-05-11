package com.sudamericana.impoexcel.model;

public class EstadoRegistro {
    private int estadoRegistro;
    private String descripcion;

    public EstadoRegistro() {
    }

    public EstadoRegistro(int estadoRegistro, String descripcion) {
        this.estadoRegistro = estadoRegistro;
        this.descripcion = descripcion;
    }

    public int getEstadoRegistro() {
        return estadoRegistro;
    }

    public void setEstadoRegistro(int estadoRegistro) {
        this.estadoRegistro = estadoRegistro;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
