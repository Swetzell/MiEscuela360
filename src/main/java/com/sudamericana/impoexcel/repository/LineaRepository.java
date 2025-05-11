package com.sudamericana.impoexcel.repository;
import com.sudamericana.impoexcel.model.Linea;
import java.util.List;

public interface LineaRepository {
    List<Linea> listarLineas();
    List<Linea> buscarLineas(String linea, String descripcion);
}
