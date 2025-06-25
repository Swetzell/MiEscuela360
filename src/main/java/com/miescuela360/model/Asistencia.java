package com.miescuela360.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "asistencias")
public class Asistencia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Alumno alumno;
    
    @Column(nullable = false)
    private LocalDate fecha;
    
    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;
    
    @Column(name = "hora_salida")
    private LocalTime horaSalida;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsistencia estado = EstadoAsistencia.PRESENTE;
    
    private String observaciones;
    
    @ManyToOne
    @JoinColumn(name = "registrado_por")
    private Usuario registradoPor;

    public enum EstadoAsistencia {
        PRESENTE,
        TARDANZA,
        AUSENTE,
        JUSTIFICADO
    }

    // Constructor vacío
    public Asistencia() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public EstadoAsistencia getEstado() {
        return estado;
    }

    public void setEstado(EstadoAsistencia estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }
} 