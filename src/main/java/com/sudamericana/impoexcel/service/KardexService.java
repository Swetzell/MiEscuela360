package com.sudamericana.impoexcel.service;
import com.sudamericana.impoexcel.model.KardexDetalle;
import com.sudamericana.impoexcel.model.Producto;
import com.sudamericana.impoexcel.repository.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KardexService {

    @Autowired
    private KardexRepository kardexRepository;

    public List<Producto> buscarProductos(String almacen, String codigo, String descripcion, String linea, String marca, int page) {
        return kardexRepository.buscarProductos(almacen, codigo, descripcion, linea, marca, page);
    }

    public List<KardexDetalle> buscarDetalleKardex(String codigoProducto, String fechaDesde, String fechaHasta) {
        return kardexRepository.buscarDetalleKardex(codigoProducto, fechaDesde, fechaHasta);
    }
}