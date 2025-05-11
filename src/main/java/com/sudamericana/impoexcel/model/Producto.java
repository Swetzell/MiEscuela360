package com.sudamericana.impoexcel.model;

import java.math.BigDecimal;
import java.util.Date;
public class Producto {
 private String codi;
    private String umed;
    private String codf;
    private String marc;
    private String descr;
    private BigDecimal stoc;
    private BigDecimal svta;
    private BigDecimal pedi;
    private BigDecimal scon;
    private BigDecimal orde;
    private String codcat;
    private String loca;
    private String orig;
    private String ubica;
    private String codalm;
    private Integer estado;
    private String msto;
    private Boolean aigv;
    private BigDecimal smin;
    private Date fe_i;
    private BigDecimal ca_i;
    private BigDecimal pr_d;
    private BigDecimal pr_s;
    private BigDecimal cped;
    private BigDecimal pcus;
    private BigDecimal pcns;
    private BigDecimal vvus;
    private Date ufad;
    private BigDecimal stoDis;
    private BigDecimal spro;
    private String codalmsel;
    private String deslinea;
    private String desslinea;
    private String desgrupo;
    private BigDecimal stod;
    //extra
    private String codigo;
    private String descripcion;
    private String marca;
    private String unidadMedida;
    private BigDecimal stock;
    private BigDecimal stockDisponible;
    private String desLinea;
    private String desSubLinea;
    private String desGrupo;
    //
    private String codNava;
    private String producto;
    private BigDecimal stockFisico;
    private BigDecimal stockTransi;
    private BigDecimal stockConsig;
    private BigDecimal stockReserF;
    private BigDecimal stockReserP;
    private BigDecimal stockDispon;

    public Producto() {
    }

   

   



    public Producto(String codi, String umed, String codf, String marc, String descr, BigDecimal stoc, BigDecimal svta,
            BigDecimal pedi, BigDecimal scon, BigDecimal orde, String codcat, String loca, String orig, String ubica,
            String codalm, Integer estado, String msto, Boolean aigv, BigDecimal smin, Date fe_i, BigDecimal ca_i,
            BigDecimal pr_d, BigDecimal pr_s, BigDecimal cped, BigDecimal pcus, BigDecimal pcns, BigDecimal vvus,
            Date ufad, BigDecimal stoDis, BigDecimal spro, String codalmsel, String deslinea, String desslinea,
            String desgrupo, BigDecimal stod, String codigo, String descripcion, String marca, String unidadMedida,
            BigDecimal stock, BigDecimal stockDisponible, String desLinea2, String desSubLinea, String desGrupo2,
            String codNava, String producto, BigDecimal stockFisico, BigDecimal stockTransi, BigDecimal stockConsig,
            BigDecimal stockReserF, BigDecimal stockReserP, BigDecimal stockDispon) {
        this.codi = codi;
        this.umed = umed;
        this.codf = codf;
        this.marc = marc;
        this.descr = descr;
        this.stoc = stoc;
        this.svta = svta;
        this.pedi = pedi;
        this.scon = scon;
        this.orde = orde;
        this.codcat = codcat;
        this.loca = loca;
        this.orig = orig;
        this.ubica = ubica;
        this.codalm = codalm;
        this.estado = estado;
        this.msto = msto;
        this.aigv = aigv;
        this.smin = smin;
        this.fe_i = fe_i;
        this.ca_i = ca_i;
        this.pr_d = pr_d;
        this.pr_s = pr_s;
        this.cped = cped;
        this.pcus = pcus;
        this.pcns = pcns;
        this.vvus = vvus;
        this.ufad = ufad;
        this.stoDis = stoDis;
        this.spro = spro;
        this.codalmsel = codalmsel;
        this.deslinea = deslinea;
        this.desslinea = desslinea;
        this.desgrupo = desgrupo;
        this.stod = stod;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.marca = marca;
        this.unidadMedida = unidadMedida;
        this.stock = stock;
        this.stockDisponible = stockDisponible;
        desLinea = desLinea2;
        this.desSubLinea = desSubLinea;
        desGrupo = desGrupo2;
        this.codNava = codNava;
        this.producto = producto;
        this.stockFisico = stockFisico;
        this.stockTransi = stockTransi;
        this.stockConsig = stockConsig;
        this.stockReserF = stockReserF;
        this.stockReserP = stockReserP;
        this.stockDispon = stockDispon;
    }







    public String getCodi() {
        return codi;
    }

    public void setCodi(String codi) {
        this.codi = codi;
    }

    public String getUmed() {
        return umed;
    }

    public void setUmed(String umed) {
        this.umed = umed;
    }

    public String getCodf() {
        return codf;
    }

    public void setCodf(String codf) {
        this.codf = codf;
    }

    public String getMarc() {
        return marc;
    }

    public void setMarc(String marc) {
        this.marc = marc;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public BigDecimal getStoc() {
        return stoc;
    }

    public void setStoc(BigDecimal stoc) {
        this.stoc = stoc;
    }

    public BigDecimal getSvta() {
        return svta;
    }

    public void setSvta(BigDecimal svta) {
        this.svta = svta;
    }

    public BigDecimal getPedi() {
        return pedi;
    }

    public void setPedi(BigDecimal pedi) {
        this.pedi = pedi;
    }

    public BigDecimal getScon() {
        return scon;
    }

    public void setScon(BigDecimal scon) {
        this.scon = scon;
    }

    public BigDecimal getOrde() {
        return orde;
    }

    public void setOrde(BigDecimal orde) {
        this.orde = orde;
    }

    public String getCodcat() {
        return codcat;
    }

    public void setCodcat(String codcat) {
        this.codcat = codcat;
    }

    public String getLoca() {
        return loca;
    }

    public void setLoca(String loca) {
        this.loca = loca;
    }

    public String getOrig() {
        return orig;
    }

    public void setOrig(String orig) {
        this.orig = orig;
    }

    public String getUbica() {
        return ubica;
    }

    public void setUbica(String ubica) {
        this.ubica = ubica;
    }

    public String getCodalm() {
        return codalm;
    }

    public void setCodalm(String codalm) {
        this.codalm = codalm;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public String getMsto() {
        return msto;
    }

    public void setMsto(String msto) {
        this.msto = msto;
    }

    public Boolean getAigv() {
        return aigv;
    }

    public void setAigv(Boolean aigv) {
        this.aigv = aigv;
    }

    public BigDecimal getSmin() {
        return smin;
    }

    public void setSmin(BigDecimal smin) {
        this.smin = smin;
    }

    public Date getFe_i() {
        return fe_i;
    }

    public void setFe_i(Date fe_i) {
        this.fe_i = fe_i;
    }

    public BigDecimal getCa_i() {
        return ca_i;
    }

    public void setCa_i(BigDecimal ca_i) {
        this.ca_i = ca_i;
    }

    public BigDecimal getPr_d() {
        return pr_d;
    }

    public void setPr_d(BigDecimal pr_d) {
        this.pr_d = pr_d;
    }

    public BigDecimal getPr_s() {
        return pr_s;
    }

    public void setPr_s(BigDecimal pr_s) {
        this.pr_s = pr_s;
    }

    public BigDecimal getCped() {
        return cped;
    }

    public void setCped(BigDecimal cped) {
        this.cped = cped;
    }

    public BigDecimal getPcus() {
        return pcus;
    }

    public void setPcus(BigDecimal pcus) {
        this.pcus = pcus;
    }

    public BigDecimal getPcns() {
        return pcns;
    }

    public void setPcns(BigDecimal pcns) {
        this.pcns = pcns;
    }

    public BigDecimal getVvus() {
        return vvus;
    }

    public void setVvus(BigDecimal vvus) {
        this.vvus = vvus;
    }

    public Date getUfad() {
        return ufad;
    }

    public void setUfad(Date ufad) {
        this.ufad = ufad;
    }

    public BigDecimal getStoDis() {
        return stoDis;
    }

    public void setStoDis(BigDecimal stoDis) {
        this.stoDis = stoDis;
    }

    public BigDecimal getSpro() {
        return spro;
    }

    public void setSpro(BigDecimal spro) {
        this.spro = spro;
    }

    public String getCodalmsel() {
        return codalmsel;
    }

    public void setCodalmsel(String codalmsel) {
        this.codalmsel = codalmsel;
    }

    public String getDeslinea() {
        return deslinea;
    }

    public void setDeslinea(String deslinea) {
        this.deslinea = deslinea;
    }

    public String getDesslinea() {
        return desslinea;
    }

    public void setDesslinea(String desslinea) {
        this.desslinea = desslinea;
    }

    public String getDesgrupo() {
        return desgrupo;
    }

    public void setDesgrupo(String desgrupo) {
        this.desgrupo = desgrupo;
    }

    public BigDecimal getStod() {
        return stod;
    }

    public void setStod(BigDecimal stod) {
        this.stod = stod;
    }



    public String getCodigo() {
        return codigo;
    }



    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }



    public String getDescripcion() {
        return descripcion;
    }



    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }



    public String getMarca() {
        return marca;
    }



    public void setMarca(String marca) {
        this.marca = marca;
    }



    public String getUnidadMedida() {
        return unidadMedida;
    }



    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }



    public BigDecimal getStock() {
        return stock;
    }



    public void setStock(BigDecimal stock) {
        this.stock = stock;
    }



    public BigDecimal getStockDisponible() {
        return stockDisponible;
    }



    public void setStockDisponible(BigDecimal stockDisponible) {
        this.stockDisponible = stockDisponible;
    }



    public String getDesLinea() {
        return desLinea;
    }



    public void setDesLinea(String desLinea) {
        this.desLinea = desLinea;
    }



    public String getDesSubLinea() {
        return desSubLinea;
    }



    public void setDesSubLinea(String desSubLinea) {
        this.desSubLinea = desSubLinea;
    }



    public String getDesGrupo() {
        return desGrupo;
    }



    public void setDesGrupo(String desGrupo) {
        this.desGrupo = desGrupo;
    }







    public String getCodNava() {
        return codNava;
    }







    public void setCodNava(String codNava) {
        this.codNava = codNava;
    }







    public String getProducto() {
        return producto;
    }







    public void setProducto(String producto) {
        this.producto = producto;
    }







    public BigDecimal getStockFisico() {
        return stockFisico;
    }







    public void setStockFisico(BigDecimal stockFisico) {
        this.stockFisico = stockFisico;
    }







    public BigDecimal getStockTransi() {
        return stockTransi;
    }







    public void setStockTransi(BigDecimal stockTransi) {
        this.stockTransi = stockTransi;
    }







    public BigDecimal getStockConsig() {
        return stockConsig;
    }







    public void setStockConsig(BigDecimal stockConsig) {
        this.stockConsig = stockConsig;
    }







    public BigDecimal getStockReserF() {
        return stockReserF;
    }







    public void setStockReserF(BigDecimal stockReserF) {
        this.stockReserF = stockReserF;
    }







    public BigDecimal getStockReserP() {
        return stockReserP;
    }







    public void setStockReserP(BigDecimal stockReserP) {
        this.stockReserP = stockReserP;
    }







    public BigDecimal getStockDispon() {
        return stockDispon;
    }







    public void setStockDispon(BigDecimal stockDispon) {
        this.stockDispon = stockDispon;
    }
    
    
}
