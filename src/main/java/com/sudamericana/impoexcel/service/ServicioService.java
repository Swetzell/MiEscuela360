package com.sudamericana.impoexcel.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sudamericana.impoexcel.model.Servicio;
import com.sudamericana.impoexcel.repository.ServicioRepository;

@Service
public class ServicioService {
    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Transactional(readOnly = true)
    public List<Servicio> listarTodos() {
        return servicioRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Servicio> buscarPorId(Long id) {
        return servicioRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Servicio> buscarPorNombre(String nombre) {
        return servicioRepository.findByNombre(nombre);
    }
    
    @Transactional(readOnly = true)
    public Optional<Servicio> buscarPorRuta(String ruta) {
        return servicioRepository.findByRuta(ruta);
    }
    
    @Transactional(readOnly = true)
    public List<Servicio> buscarPorPrefijo(String rutaPrefix) {
        return servicioRepository.findByRutaStartingWith(rutaPrefix);
    }
    
    @Transactional
    public Servicio guardar(Servicio servicio) {
        // Verificar si es una actualización o creación
        if (servicio.getId() != null && servicio.getId() > 0) {
            // Es una actualización - buscar el servicio existente
            Optional<Servicio> existente = buscarPorId(servicio.getId());
            if (existente.isPresent()) {
                Servicio servicioExistente = existente.get();
                // Actualizar sólo los campos modificables
                servicioExistente.setNombre(servicio.getNombre());
                servicioExistente.setDescripcion(servicio.getDescripcion());
                servicioExistente.setRuta(servicio.getRuta());
                // Guardar la entidad actualizada
                return servicioRepository.save(servicioExistente);
            }
        }
        
        // Es una creación o no se encontró el servicio existente
        return servicioRepository.save(servicio);
    }
    
    @Transactional
    public void eliminar(Long id) {
        // Primero eliminar las relaciones
        eliminarRelacionesConUsuarios(id);
        // Luego eliminar el servicio
        servicioRepository.deleteById(id);
    }

@Transactional
public void eliminarRelacionesConUsuarios(Long servicioId) {
    jdbcTemplate.update("DELETE FROM usuario_servicios WHERE servicio_id = ?", servicioId);
}
}