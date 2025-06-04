package com.miescuela360.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "alumnos")
public class Alumno {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String apellido;
    
    @Column(nullable = false, unique = true)
    private String dni;
    
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    
    private String direccion;
    
    private String telefono;
    
    @Column(nullable = true)
    private String email;
    
    // Mantener estos campos por compatibilidad con datos existentes
    @Column(name = "grado_actual")
    private String gradoActual;
    
    @Column(name = "seccion_actual")
    private String seccionActual;
    
    @ManyToOne
    @JoinColumn(name = "grado_id")
    private Grado grado;
    
    @ManyToOne
    @JoinColumn(name = "seccion_id")
    private Seccion seccion;
    
    @Column(name = "estado_academico")
    @Enumerated(EnumType.STRING)
    private EstadoAcademico estadoAcademico = EstadoAcademico.ACTIVO;
    
    @ManyToOne
    @JoinColumn(name = "padre_id")
    private Padre padre;
    
    @ManyToOne
    @JoinColumn(name = "madre_id")
    private Madre madre;
    
    @Column(name = "telefono_emergencia")
    private String telefonoEmergencia;
    
    private boolean activo = true;
    
    @Column(name = "fecha_inscripcion")
    private LocalDate fechaInscripcion;
    
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private Set<Asistencia> asistencias = new HashSet<>();
    
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private Set<Pago> pagos = new HashSet<>();
    
    @Column(name = "monto_mensual")
    private Double montoMensual;
    
    @Column(name = "ultimo_pago")
    private LocalDate ultimoPago;

    public enum EstadoAcademico {
        ACTIVO,
        SUSPENDIDO,
        RETIRADO,
        GRADUADO
    }

    // Constructor vacío
    public Alumno() {
    }

    // Getters y Setters
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGradoActual() {
        return gradoActual;
    }

    public void setGradoActual(String gradoActual) {
        this.gradoActual = gradoActual;
    }

    public String getSeccionActual() {
        return seccionActual;
    }

    public void setSeccionActual(String seccionActual) {
        this.seccionActual = seccionActual;
    }

    public EstadoAcademico getEstadoAcademico() {
        return estadoAcademico;
    }

    public void setEstadoAcademico(EstadoAcademico estadoAcademico) {
        this.estadoAcademico = estadoAcademico;
    }

    public Padre getPadre() {
        return padre;
    }

    public void setPadre(Padre padre) {
        this.padre = padre;
    }

    public Madre getMadre() {
        return madre;
    }

    public void setMadre(Madre madre) {
        this.madre = madre;
    }

    public String getTelefonoEmergencia() {
        return telefonoEmergencia;
    }

    public void setTelefonoEmergencia(String telefonoEmergencia) {
        this.telefonoEmergencia = telefonoEmergencia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public Set<Asistencia> getAsistencias() {
        return asistencias;
    }

    public void setAsistencias(Set<Asistencia> asistencias) {
        this.asistencias = asistencias;
    }

    public Set<Pago> getPagos() {
        return pagos;
    }

    public void setPagos(Set<Pago> pagos) {
        this.pagos = pagos;
    }

    public Double getMontoMensual() {
        return montoMensual;
    }

    public void setMontoMensual(Double montoMensual) {
        this.montoMensual = montoMensual;
    }

    public LocalDate getUltimoPago() {
        return ultimoPago;
    }

    public void setUltimoPago(LocalDate ultimoPago) {
        this.ultimoPago = ultimoPago;
    }

    public Grado getGrado() {
        return grado;
    }

    public void setGrado(Grado grado) {
        this.grado = grado;
        if (grado != null) {
            this.gradoActual = grado.getNombre(); // Mantener sincronizado para compatibilidad
        }
    }

    public Seccion getSeccion() {
        return seccion;
    }

    public void setSeccion(Seccion seccion) {
        this.seccion = seccion;
        if (seccion != null) {
            this.seccionActual = seccion.getNombre(); // Mantener sincronizado para compatibilidad
        }
    }
}