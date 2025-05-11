package com.sudamericana.impoexcel.model;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class ReporteABCFiltro {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;
    private String tipoReporte;
    private String vendedor = "%";
    private String estado = "%";
    private String duvDesde = "0";
    private String duvHasta = "10000";
    private String condicionPago = "%";
    private String formato;
    //producto
    private String marca;
    private String pctCantidad;
    private String pctMovimiento;
    private String pctVenta;
    private String curvaA;
    private String curvaB;
    private String curvaC;

    public ReporteABCFiltro() {
        LocalDate now = LocalDate.now();
        this.fechaInicio = now.withDayOfMonth(1);
        this.fechaFin = now;
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

    public String getVendedor() {
        return vendedor == null || vendedor.isEmpty() ? "%" : vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor == null || vendedor.isEmpty() ? "%" : vendedor;
    }

    public String getEstado() {
        return estado == null || estado.isEmpty() ? "%" : estado;
    }

    public void setEstado(String estado) {
        this.estado = estado == null || estado.isEmpty() ? "%" : estado;
    }

    public String getDuvDesde() {
        return duvDesde == null || duvDesde.isEmpty() ? "0" : duvDesde;
    }

    public void setDuvDesde(String duvDesde) {
        this.duvDesde = duvDesde == null || duvDesde.isEmpty() ? "0" : duvDesde;
    }

    public String getDuvHasta() {
        return duvHasta == null || duvHasta.isEmpty() ? "10000" : duvHasta;
    }

    public void setDuvHasta(String duvHasta) {
        this.duvHasta = duvHasta == null || duvHasta.isEmpty() ? "10000" : duvHasta;
    }

    public String getCondicionPago() {
        return condicionPago == null || condicionPago.isEmpty() ? "%" : condicionPago;
    }

    public void setCondicionPago(String condicionPago) {
        this.condicionPago = condicionPago == null || condicionPago.isEmpty() ? "%" : condicionPago;
    }
    

    public void setTipoReporte(String tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public String getTipoReporte() {
        return tipoReporte;
    }

    
//producto
public String getMarca() {
    return marca;
}

public void setMarca(String marca) {
    this.marca = marca;
}

public String getPctCantidad() {
    return pctCantidad;
}

public void setPctCantidad(String pctCantidad) {
    this.pctCantidad = pctCantidad;
}

public String getPctMovimiento() {
    return pctMovimiento;
}

public void setPctMovimiento(String pctMovimiento) {
    this.pctMovimiento = pctMovimiento;
}

public String getPctVenta() {
    return pctVenta;
}

public void setPctVenta(String pctVenta) {
    this.pctVenta = pctVenta;
}

public String getCurvaA() {
    return curvaA;
}

public void setCurvaA(String curvaA) {
    this.curvaA = curvaA;
}

public String getCurvaB() {
    return curvaB;
}

public void setCurvaB(String curvaB) {
    this.curvaB = curvaB;
}

public String getCurvaC() {
    return curvaC;
}

public void setCurvaC(String curvaC) {
    this.curvaC = curvaC;
}

public String getFormato() {
    return formato;
}

public void setFormato(String formato) {
    this.formato = formato;
}
    
}