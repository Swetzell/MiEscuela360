package com.miescuela360.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Rol {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nombre;
    
    @Column(length = 1000)
    private String descripcion;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_id"))
    @Column(name = "permiso")
    @Enumerated(EnumType.STRING)
    private Set<Permiso> permisos = new HashSet<>();
    
    @ManyToMany(mappedBy = "roles")
    private Set<Usuario> usuarios = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "rol_servicios",
        joinColumns = @JoinColumn(name = "rol_id"),
        inverseJoinColumns = @JoinColumn(name = "servicio_id")
    )
    private Set<Servicio> servicios = new HashSet<>();
    
    // Constructor por defecto
    public Rol() {
    }
    
    // Constructor con nombre
    public Rol(String nombre) {
        this.nombre = nombre;
    }
    
    // Getters y Setters existentes
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
    
    public Set<Usuario> getUsuarios() {
        return usuarios;
    }
    
    public void setUsuarios(Set<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
    
    public Set<Servicio> getServicios() {
        return servicios;
    }
    
    public void setServicios(Set<Servicio> servicios) {
        this.servicios = servicios;
    }
    
    // Nuevos métodos para manejar permisos
    public Set<Permiso> getPermisos() {
        return permisos;
    }
    
    public void setPermisos(Set<Permiso> permisos) {
        this.permisos = permisos;
    }
    
    public void agregarPermiso(Permiso permiso) {
        permisos.add(permiso);
    }
    
    public void eliminarPermiso(Permiso permiso) {
        permisos.remove(permiso);
    }
    
    public boolean tienePermiso(Permiso permiso) {
        return permisos.contains(permiso);
    }
}