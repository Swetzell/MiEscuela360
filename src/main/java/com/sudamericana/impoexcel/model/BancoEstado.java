package com.sudamericana.impoexcel.model;

public class BancoEstado {
    private Integer estado;
    private String descrip;
    
    // Constructores
    public BancoEstado() {
    }
    
    public BancoEstado(Integer estado, String descrip) {
        this.estado = estado;
        this.descrip = descrip;
    }
    
    // Getters y Setters
    public Integer getEstado() {
        return estado;
    }
    
    public void setEstado(Integer estado) {
        this.estado = estado;
    }
    
    public String getDescrip() {
        return descrip;
    }
    
    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }
    
    @Override
    public String toString() {
        return "BancoEstado{" +
                "estado=" + estado +
                ", descrip='" + descrip + '\'' +
                '}';
    }
}