package com.sudamericana.impoexcel.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agentes_retenedores")
public class AgenteRetenedor {
    
    @Id
    @Column(length = 11)
    private String ruc;
    
    @Column(name = "razon_social", nullable = false, length = 255)
    private String razonSocial;
    
    @Column(name = "a_partir_del")
    private LocalDate aPartirDel;
    
    @Column(length = 100)
    private String resolucion;
    
    @Column(name = "fecha_consulta")
    private LocalDateTime fechaConsulta;
    
    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    
    // Getters y setters
    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public LocalDate getAPartirDel() {
        return aPartirDel;
    }

    public void setAPartirDel(LocalDate aPartirDel) {
        this.aPartirDel = aPartirDel;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public LocalDateTime getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(LocalDateTime fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaActualizacion = LocalDateTime.now();
    }
}