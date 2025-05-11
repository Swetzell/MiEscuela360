package com.sudamericana.impoexcel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes_sunat")
public class ClienteSunat {
    
    @Id
    @Column(length = 11)
    private String ruc;
    
    @Column(name = "razon_social", nullable = false, length = 255)
    private String razonSocial;
    
    @Column(name = "nombre_comercial", length = 255)
    private String nombreComercial;
    
    @Column(nullable = false, length = 50)
    private String estado;
    
    @Column(nullable = false, length = 50)
    private String condicion;
    
    @Column(length = 500)
    private String direccion;
    
    @Column(length = 50)
    private String ubigeo;
    
    @Column(name = "tipo_contribuyente", length = 100)
    private String tipoContribuyente;
    
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

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getUbigeo() {
        return ubigeo;
    }

    public void setUbigeo(String ubigeo) {
        this.ubigeo = ubigeo;
    }

    public String getTipoContribuyente() {
        return tipoContribuyente;
    }

    public void setTipoContribuyente(String tipoContribuyente) {
        this.tipoContribuyente = tipoContribuyente;
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