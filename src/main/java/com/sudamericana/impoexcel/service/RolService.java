package com.sudamericana.impoexcel.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sudamericana.impoexcel.model.Rol;
import com.sudamericana.impoexcel.repository.RolRepository;

@Service
public class RolService {
@Autowired
    private RolRepository rolRepository;
    
    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }
}
