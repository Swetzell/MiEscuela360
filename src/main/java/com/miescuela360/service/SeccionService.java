package com.miescuela360.service;

import com.miescuela360.model.Seccion;
import com.miescuela360.repository.SeccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SeccionService {
    
    @Autowired
    private SeccionRepository seccionRepository;
    
    @Transactional(readOnly = true)
    public List<Seccion> findAll() {
        return seccionRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Seccion> findAllActive() {
        return seccionRepository.findByActivoTrue();
    }
    
    @Transactional(readOnly = true)
    public Optional<Seccion> findById(Long id) {
        return seccionRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Seccion findByNombre(String nombre) {
        return seccionRepository.findByNombre(nombre);
    }
    
    @Transactional
    public Seccion save(Seccion seccion) {
        return seccionRepository.save(seccion);
    }
    
    @Transactional
    public void delete(Long id) {
        seccionRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByNombre(String nombre) {
        return seccionRepository.existsByNombre(nombre);
    }
    
    /**
     * Desactiva una sección sin eliminarla físicamente
     */
    @Transactional
    public void disable(Long id) {
        Optional<Seccion> optSeccion = seccionRepository.findById(id);
        if (optSeccion.isPresent()) {
            Seccion seccion = optSeccion.get();
            seccion.setActivo(false);
            seccionRepository.save(seccion);
        }
    }

    /**
     * Activa una sección previamente desactivada
     */
    @Transactional
    public void enable(Long id) {
        Optional<Seccion> optSeccion = seccionRepository.findById(id);
        if (optSeccion.isPresent()) {
            Seccion seccion = optSeccion.get();
            seccion.setActivo(true);
            seccionRepository.save(seccion);
        }
    }

    /**
     * Alterna el estado activo/inactivo de una sección
     */
    @Transactional
    public void toggleActive(Long id) {
        Optional<Seccion> optSeccion = seccionRepository.findById(id);
        if (optSeccion.isPresent()) {
            Seccion seccion = optSeccion.get();
            seccion.setActivo(!seccion.getActivo());
            seccionRepository.save(seccion);
        }
    }
    
    /**
     * Método para inicializar datos por defecto si no existen
     */
    @Transactional
    public void initDefaultData() {
        // Secciones típicas en Perú
        String[] seccionesDefault = {"A", "B", "C", "D"};
        
        for (String nombre : seccionesDefault) {
            if (!existsByNombre(nombre)) {
                Seccion seccion = new Seccion(nombre);
                seccion.setDescripcion("Sección " + nombre);
                seccion.setActivo(true);
                save(seccion);
            }
        }
    }
    
    /**
     * Inicializa las secciones del sistema, creando las secciones por defecto si no existen
     */
    @Transactional
    public void inicializarSecciones() {
        initDefaultData();
    }
}
