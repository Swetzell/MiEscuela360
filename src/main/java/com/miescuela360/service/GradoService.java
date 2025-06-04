package com.miescuela360.service;

import com.miescuela360.model.Grado;
import com.miescuela360.repository.GradoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GradoService {
    
    @Autowired
    private GradoRepository gradoRepository;
    
    @Transactional(readOnly = true)
    public List<Grado> findAll() {
        return gradoRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Grado> findAllActive() {
        return gradoRepository.findByActivoTrue();
    }
    
    @Transactional(readOnly = true)
    public Optional<Grado> findById(Long id) {
        return gradoRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Grado findByNombre(String nombre) {
        return gradoRepository.findByNombre(nombre);
    }
    
    @Transactional
    public Grado save(Grado grado) {
        return gradoRepository.save(grado);
    }
    
    @Transactional
    public void delete(Long id) {
        gradoRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByNombre(String nombre) {
        return gradoRepository.existsByNombre(nombre);
    }
    
    /**
     * Desactiva un grado sin eliminarlo físicamente
     */
    @Transactional
    public void disable(Long id) {
        Optional<Grado> optGrado = gradoRepository.findById(id);
        if (optGrado.isPresent()) {
            Grado grado = optGrado.get();
            grado.setActivo(false);
            gradoRepository.save(grado);
        }
    }

    /**
     * Activa un grado previamente desactivado
     */
    @Transactional
    public void enable(Long id) {
        Optional<Grado> optGrado = gradoRepository.findById(id);
        if (optGrado.isPresent()) {
            Grado grado = optGrado.get();
            grado.setActivo(true);
            gradoRepository.save(grado);
        }
    }

    /**
     * Alterna el estado activo/inactivo de un grado
     */
    @Transactional
    public void toggleActive(Long id) {
        Optional<Grado> optGrado = gradoRepository.findById(id);
        if (optGrado.isPresent()) {
            Grado grado = optGrado.get();
            grado.setActivo(!grado.getActivo());
            gradoRepository.save(grado);
        }
    }
    
    /**
     * Método para inicializar datos por defecto si no existen
     */
    @Transactional
    public void initDefaultData() {
        // Grados típicos de primaria en Perú
        String[] gradosDefault = {"1°", "2°", "3°", "4°", "5°", "6°"};
        
        for (String nombre : gradosDefault) {
            if (!existsByNombre(nombre)) {
                Grado grado = new Grado(nombre);
                grado.setDescripcion(nombre + " de Primaria");
                grado.setActivo(true);
                save(grado);
            }
        }
    }
    
    /**
     * Inicializa los grados del sistema, creando los grados por defecto si no existen
     */
    @Transactional
    public void inicializarGrados() {
        initDefaultData();
    }
}
