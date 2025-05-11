package com.miescuela360.service;

import com.miescuela360.model.Alumno;
import com.miescuela360.repository.AlumnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlumnoService {

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Transactional(readOnly = true)
    public List<Alumno> findAll() {
        return alumnoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Alumno> findById(Long id) {
        return alumnoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Alumno> findByDni(String dni) {
        return alumnoRepository.findByDni(dni);
    }

    @Transactional
    public Alumno save(Alumno alumno) {
        return alumnoRepository.save(alumno);
    }

    @Transactional
    public void deleteById(Long id) {
        alumnoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByDni(String dni) {
        return alumnoRepository.existsByDni(dni);
    }

    @Transactional(readOnly = true)
    public List<Alumno> findByGradoAndSeccion(String grado, String seccion) {
        return alumnoRepository.findByGradoActualAndSeccionActual(grado, seccion);
    }

    public void activarDesactivar(Long id) {
        alumnoRepository.findById(id).ifPresent(alumno -> {
            alumno.setActivo(!alumno.isActivo());
            alumnoRepository.save(alumno);
        });
    }
} 