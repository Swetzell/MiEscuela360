package com.sudamericana.impoexcel.service;

import com.sudamericana.impoexcel.model.SubLinea;
import com.sudamericana.impoexcel.repository.SubLineaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SubLineaService {

    @Autowired
    private SubLineaRepository subLineaRepository;

    public List<SubLinea> listarSubLineas() {
        return subLineaRepository.listarSubLineas();
    }

    public List<SubLinea> buscarSubLineas(String sublinea, String descripcion) {
        return subLineaRepository.buscarSubLineas(sublinea, descripcion);
    }
}