package com.miescuela360.service;

import com.miescuela360.model.Madre;
import com.miescuela360.model.Padre;
import com.miescuela360.repository.MadreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MadreService {

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<Madre> findAll() {
        return madreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Madre> findById(Long id) {
        return madreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Madre> findByDni(String dni) {
        return madreRepository.findByDni(dni);
    }

    @Transactional
    public Madre save(Madre madre) {
        Madre madreAnterior = null;
        String accion = "CREAR";
        
        if (madre.getId() != null) {
            madreAnterior = madreRepository.findById(madre.getId()).orElse(null);
            accion = "ACTUALIZAR";
        }
        
        Madre madreGuardado = madreRepository.save(madre);
        
        // Registrar auditoría
        auditoriaService.registrarAccion(accion, madreGuardado, madreAnterior);
        
        return madreGuardado;
    }

    @Transactional
    public void deleteById(Long id) {
        Optional<Madre> madreOpt = madreRepository.findById(id);
        if (madreOpt.isPresent()) {
            Madre madre = madreOpt.get();
            madreRepository.deleteById(id);
            
            auditoriaService.registrarAccion("ELIMINAR", madre, madre);
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByDni(String dni) {
        return madreRepository.existsByDni(dni);
    }
} 