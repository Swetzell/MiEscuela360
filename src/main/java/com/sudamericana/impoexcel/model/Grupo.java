package com.sudamericana.impoexcel.model;

public class Grupo {
    private String grupo;
    private String descripcion;

    public Grupo() {
    }


    public Grupo(String grupo, String descripcion) {
        this.grupo = grupo;
        this.descripcion = descripcion;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}