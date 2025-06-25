package com.miescuela360.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "grados")
public class Grado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = false)
    private String nombre;
    
    @Column
    private String descripcion;
    
    @Column
    private Boolean activo = true;
    
    @OneToMany(mappedBy = "grado")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Alumno> alumnos;
    
    public Grado() {
    }
    
    public Grado(String nombre) {
        this.nombre = nombre;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public List<Alumno> getAlumnos() {
        return alumnos;
    }
    
    public void setAlumnos(List<Alumno> alumnos) {
        this.alumnos = alumnos;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}
