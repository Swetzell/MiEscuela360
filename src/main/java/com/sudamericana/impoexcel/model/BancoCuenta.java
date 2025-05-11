package com.sudamericana.impoexcel.model;

public class BancoCuenta {
    private String nrocta;
    private String descrip;
    private String codbco;
    private String nombco;
    private String mone;
    
    // Constructores
    public BancoCuenta() {
    }
    
    public BancoCuenta(String nrocta, String descrip, String codbco, String nombco, String mone) {
        this.nrocta = nrocta;
        this.descrip = descrip;
        this.codbco = codbco;
        this.nombco = nombco;
        this.mone = mone;
    }
    
    // Getters y Setters
    public String getNrocta() {
        return nrocta;
    }
    
    public void setNrocta(String nrocta) {
        this.nrocta = nrocta;
    }
    
    public String getDescrip() {
        return descrip;
    }
    
    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }
    
    public String getCodbco() {
        return codbco;
    }
    
    public void setCodbco(String codbco) {
        this.codbco = codbco;
    }
    
    public String getNombco() {
        return nombco;
    }
    
    public void setNombco(String nombco) {
        this.nombco = nombco;
    }
    
    public String getMone() {
        return mone;
    }
    
    public void setMone(String mone) {
        this.mone = mone;
    }
    
    @Override
    public String toString() {
        return "BancoCuenta{" +
                "nrocta='" + nrocta + '\'' +
                ", descrip='" + descrip + '\'' +
                ", codbco='" + codbco + '\'' +
                ", nombco='" + nombco + '\'' +
                ", mone='" + mone + '\'' +
                '}';
    }
}