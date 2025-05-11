package com.sudamericana.impoexcel.model;


import java.util.ArrayList;
import java.util.List;


public class ReposicionFiltro {
    private String codAlm = "00";
    private String marca = "0000";
    private Integer mesesVenta = 12;
    private Integer diasDemoraProveedor = 30;
    private Integer factorDiasSeguridad = 30;
    
    private List<String> estados = new ArrayList<>();
    
    public ReposicionFiltro() {
        estados.add("TODOS");
    }
    
    public boolean isReponerSelected() {
        return estados.contains("REPONER") || estados.contains("TODOS");
    }
    
    public boolean isBajoStockSelected() {
        return estados.contains("BAJO STOCK") || estados.contains("TODOS");
    }
    
    public boolean isSobreStockSelected() {
        return estados.contains("SOBRE STOCK") || estados.contains("TODOS");
    }
    
    public boolean isTodosSelected() {
        return estados.contains("TODOS");
    }
    
    public boolean isOnlyStateSelected(String state) {
        return estados.size() == 1 && estados.contains(state);
    }
    
    public String getEstadosClausulaWhere() {
        if (estados == null || estados.isEmpty() || estados.contains("TODOS")) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder("where Estado in (");
        for (int i = 0; i < estados.size(); i++) {
            sb.append("'").append(estados.get(i)).append("'");
            if (i < estados.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        
        return sb.toString();
    }

    public String getCodAlm() {
        return codAlm;
    }

    public void setCodAlm(String codAlm) {
        this.codAlm = codAlm;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Integer getMesesVenta() {
        return mesesVenta;
    }

    public void setMesesVenta(Integer mesesVenta) {
        this.mesesVenta = mesesVenta;
    }

    public Integer getDiasDemoraProveedor() {
        return diasDemoraProveedor;
    }

    public void setDiasDemoraProveedor(Integer diasDemoraProveedor) {
        this.diasDemoraProveedor = diasDemoraProveedor;
    }

    public Integer getFactorDiasSeguridad() {
        return factorDiasSeguridad;
    }

    public void setFactorDiasSeguridad(Integer factorDiasSeguridad) {
        this.factorDiasSeguridad = factorDiasSeguridad;
    }

    public List<String> getEstados() {
        return estados;
    }

    public void setEstados(List<String> estados) {
        this.estados = estados;
    }

    
}