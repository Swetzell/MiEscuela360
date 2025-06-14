package com.miescuela360.service;

import com.miescuela360.model.Padre;
import com.miescuela360.repository.PadreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PadreService {

    @Autowired
    private PadreRepository padreRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<Padre> findAll() {
        return padreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Padre> findById(Long id) {
        return padreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Padre> findByDni(String dni) {
        return padreRepository.findByDni(dni);
    }

    @Transactional
    public Padre save(Padre padre) {
        Padre padreAnterior = null;
        String accion = "CREAR";
        
        if (padre.getId() != null) {
            padreAnterior = padreRepository.findById(padre.getId()).orElse(null);
            accion = "ACTUALIZAR";
        }
        
        Padre padreGuardado = padreRepository.save(padre);
        
        // Registrar auditoría
        auditoriaService.registrarAccion(accion, padreGuardado, padreAnterior);
        
        return padreGuardado;
    }

    @Transactional
    public void deleteById(Long id) {
        Optional<Padre> padreOpt = padreRepository.findById(id);
        if (padreOpt.isPresent()) {
            Padre padre = padreOpt.get();
            padreRepository.deleteById(id);
            
            auditoriaService.registrarAccion("ELIMINAR", padre, padre);
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByDni(String dni) {
        return padreRepository.existsByDni(dni);
    }
} 