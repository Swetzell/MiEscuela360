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
    
    @Autowired
    private AuditoriaService auditoriaService;
    
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
        Seccion seccionAnterior = null;
        String accion = "CREAR";
        
        if (seccion.getId() != null) {
            seccionAnterior = seccionRepository.findById(seccion.getId()).orElse(null);
            accion = "ACTUALIZAR";
        }
        
        Seccion seccionGuardada = seccionRepository.save(seccion);
        
        // Registrar auditoría
        auditoriaService.registrarAccion(accion, seccionGuardada, seccionAnterior);
        
        return seccionGuardada;
    }
    
    @Transactional
    public void delete(Long id) {
        Optional<Seccion> seccionOpt = seccionRepository.findById(id);
        if (seccionOpt.isPresent()) {
            Seccion seccion = seccionOpt.get();
            seccionRepository.deleteById(id);
            
            // Registrar auditoría
            auditoriaService.registrarAccion("ELIMINAR", seccion, seccion);
        }
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
            
            // Crear una copia del estado anterior para auditoría
            Seccion seccionAnterior = new Seccion();
            seccionAnterior.setId(seccion.getId());
            seccionAnterior.setNombre(seccion.getNombre());
            seccionAnterior.setDescripcion(seccion.getDescripcion());
            seccionAnterior.setActivo(seccion.getActivo()); // Estado anterior
            
            // Cambiar el estado
            seccion.setActivo(false);
            seccionRepository.save(seccion);
            
            // Registrar auditoría con estado anterior y nuevo
            auditoriaService.registrarAccion("DESACTIVAR", seccion, seccionAnterior);
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
            
            // Crear una copia del estado anterior para auditoría
            Seccion seccionAnterior = new Seccion();
            seccionAnterior.setId(seccion.getId());
            seccionAnterior.setNombre(seccion.getNombre());
            seccionAnterior.setDescripcion(seccion.getDescripcion());
            seccionAnterior.setActivo(seccion.getActivo()); // Estado anterior
            
            // Cambiar el estado
            seccion.setActivo(true);
            seccionRepository.save(seccion);
            
            // Registrar auditoría con estado anterior y nuevo
            auditoriaService.registrarAccion("ACTIVAR", seccion, seccionAnterior);
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
            
            // Crear una copia del estado anterior para auditoría
            Seccion seccionAnterior = new Seccion();
            seccionAnterior.setId(seccion.getId());
            seccionAnterior.setNombre(seccion.getNombre());
            seccionAnterior.setDescripcion(seccion.getDescripcion());
            seccionAnterior.setActivo(seccion.getActivo()); // Estado anterior
            
            // Determinar la acción según el estado actual
            String accion = seccion.getActivo() ? "DESACTIVAR" : "ACTIVAR";
            
            // Cambiar el estado
            seccion.setActivo(!seccion.getActivo());
            seccionRepository.save(seccion);
            
            // Registrar auditoría
            auditoriaService.registrarAccion(accion, seccion, seccionAnterior);
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
                Seccion seccion = new Seccion();
                seccion.setNombre(nombre);  
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