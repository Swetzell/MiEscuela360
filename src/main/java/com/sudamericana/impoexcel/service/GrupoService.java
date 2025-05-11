package com.sudamericana.impoexcel.service;

import com.sudamericana.impoexcel.model.Grupo;
import com.sudamericana.impoexcel.repository.GrupoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository;

    public List<Grupo> listarGrupos() {
        return grupoRepository.listarGrupos();
    }

    public List<Grupo> buscarGrupos(String grupo, String descripcion) {
        return grupoRepository.buscarGrupos(grupo, descripcion);
    }
}
