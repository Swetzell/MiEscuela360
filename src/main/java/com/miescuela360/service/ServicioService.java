package com.miescuela360.service;

import com.miescuela360.model.Servicio;
import com.miescuela360.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Transactional(readOnly = true)
    public List<Servicio> findAll() {
        return servicioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Servicio> findById(Long id) {
        return servicioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Servicio> findByCodigo(String codigo) {
        return servicioRepository.findByCodigo(codigo);
    }

    @Transactional
    public Servicio save(Servicio servicio) {
        return servicioRepository.save(servicio);
    }

    @Transactional
    public void deleteById(Long id) {
        servicioRepository.deleteById(id);
    }

    @Transactional
    public boolean existsByCodigo(String codigo) {
        return servicioRepository.existsByCodigo(codigo);
    }

    @Transactional
    public void activarDesactivar(Long id) {
        servicioRepository.findById(id).ifPresent(servicio -> {
            servicio.setActivo(!servicio.isActivo());
            servicioRepository.save(servicio);
        });
    }
} 