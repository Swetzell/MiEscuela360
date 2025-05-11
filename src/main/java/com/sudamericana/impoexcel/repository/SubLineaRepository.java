package com.sudamericana.impoexcel.repository;

import com.sudamericana.impoexcel.model.SubLinea;
import java.util.List;

public interface SubLineaRepository {
    List<SubLinea> listarSubLineas();
    List<SubLinea> buscarSubLineas(String sublinea, String descripcion);
}
