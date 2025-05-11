package com.sudamericana.impoexcel.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.sudamericana.impoexcel.model.Grupo;

@Repository
public interface GrupoRepository {
    List<Grupo> listarGrupos();
    List<Grupo> buscarGrupos(String grupo, String descripcion);
}
