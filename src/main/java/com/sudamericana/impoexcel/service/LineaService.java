package com.sudamericana.impoexcel.service;

import com.sudamericana.impoexcel.model.Linea;
import com.sudamericana.impoexcel.repository.LineaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LineaService {

    @Autowired
    private LineaRepository lineaRepository;

    public List<Linea> listarLineas() {
        return lineaRepository.listarLineas();
    }

    public List<Linea> buscarLineas(String linea, String descripcion) {
        return lineaRepository.buscarLineas(linea, descripcion);
    }
}
