package com.sudamericana.impoexcel.model;

public class Linea {
    private String linea;
    private String descripcion;
    private String abreviatura;
    private boolean deCompra;
    private boolean deVenta;
    private int orden;
    private EstadoRegistro estadoRegistro;
    private String mensaje;
    private String controlSesion;

    public Linea() {
    }


    public Linea(String linea, String descripcion, String abreviatura, boolean deCompra, boolean deVenta, int orden, EstadoRegistro estadoRegistro, String mensaje, String controlSesion) {
        this.linea = linea;
        this.descripcion = descripcion;
        this.abreviatura = abreviatura;
        this.deCompra = deCompra;
        this.deVenta = deVenta;
        this.orden = orden;
        this.estadoRegistro = estadoRegistro;
        this.mensaje = mensaje;
        this.controlSesion = controlSesion;
    }


    public String getLinea() {
        return linea;
    }


    public void setLinea(String linea) {
        this.linea = linea;
    }


    public String getDescripcion() {
        return descripcion;
    }


    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    public String getAbreviatura() {
        return abreviatura;
    }


    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }


    public boolean isDeCompra() {
        return deCompra;
    }


    public void setDeCompra(boolean deCompra) {
        this.deCompra = deCompra;
    }


    public boolean isDeVenta() {
        return deVenta;
    }


    public void setDeVenta(boolean deVenta) {
        this.deVenta = deVenta;
    }


    public int getOrden() {
        return orden;
    }


    public void setOrden(int orden) {
        this.orden = orden;
    }


    public EstadoRegistro getEstadoRegistro() {
        return estadoRegistro;
    }


    public void setEstadoRegistro(EstadoRegistro estadoRegistro) {
        this.estadoRegistro = estadoRegistro;
    }


    public String getMensaje() {
        return mensaje;
    }


    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }


    public String getControlSesion() {
        return controlSesion;
    }


    public void setControlSesion(String controlSesion) {
        this.controlSesion = controlSesion;
    }

    
}
