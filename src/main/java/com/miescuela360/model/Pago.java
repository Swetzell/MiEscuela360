package com.miescuela360.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "pagos")
public class Pago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;
    
    @Column(nullable = false)
    private LocalDate fecha;
    
    @Column(nullable = false)
    private BigDecimal monto;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPago tipo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estado = EstadoPago.PENDIENTE;
    
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;
    
    @Column(name = "fecha_pago")
    private LocalDate fechaPago;
    
    private String observaciones;
    
    @ManyToOne
    @JoinColumn(name = "registrado_por")
    private Usuario registradoPor;
    
    @Column(name = "comprobante_url")
    private String comprobanteUrl;

    public enum TipoPago {
        PENSION,
        MATRICULA,
        OTRO
    }

    public enum EstadoPago {
        PENDIENTE,
        PAGADO,
        VENCIDO,
        ANULADO
    }

    // Constructor vacío
    public Pago() {
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

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public TipoPago getTipo() {
        return tipo;
    }

    public void setTipo(TipoPago tipo) {
        this.tipo = tipo;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
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

    public String getComprobanteUrl() {
        return comprobanteUrl;
    }

    public void setComprobanteUrl(String comprobanteUrl) {
        this.comprobanteUrl = comprobanteUrl;
    }
} 