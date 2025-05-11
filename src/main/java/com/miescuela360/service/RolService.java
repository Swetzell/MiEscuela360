package com.miescuela360.service;

import com.miescuela360.model.Rol;
import com.miescuela360.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    @Transactional(readOnly = true)
    public List<Rol> findAll() {
        return rolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Rol> findById(Long id) {
        return rolRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Rol> findByNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    @Transactional
    public Rol save(Rol rol) {
        return rolRepository.save(rol);
    }

    @Transactional
    public void deleteById(Long id) {
        rolRepository.deleteById(id);
    }

    @Transactional
    public boolean existsByNombre(String nombre) {
        return rolRepository.existsByNombre(nombre);
    }
} 