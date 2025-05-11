package com.sudamericana.impoexcel.model;

public class BancoEntidad {
    private String entidad;
    private String descripcion;
    
    // Constructores
    public BancoEntidad() {
    }
    
    public BancoEntidad(String entidad, String descripcion) {
        this.entidad = entidad;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public String getEntidad() {
        return entidad;
    }
    
    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    @Override
    public String toString() {
        return "BancoEntidad{" +
                "entidad='" + entidad + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}