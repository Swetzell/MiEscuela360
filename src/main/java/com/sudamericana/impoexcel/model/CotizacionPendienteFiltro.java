package com.sudamericana.impoexcel.model;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class CotizacionPendienteFiltro {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;
    
    private String codAlm = "";
    
    public CotizacionPendienteFiltro() {
        fechaInicio = LocalDate.now().withDayOfMonth(1);
        fechaFin = LocalDate.now();
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getCodAlm() {
        return codAlm;
    }

    public void setCodAlm(String codAlm) {
        this.codAlm = codAlm;
    }

    
}
