package com.miescuela360.service;

import com.miescuela360.model.Asistencia;
import com.miescuela360.model.Alumno;
import com.miescuela360.repository.AsistenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<Asistencia> findAll() {
        return asistenciaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Asistencia> findById(Long id) {
        return asistenciaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Asistencia> findByFecha(LocalDate fecha) {
        return asistenciaRepository.findByFecha(fecha);
    }

    @Transactional(readOnly = true)
    public List<Asistencia> findByAlumno(Alumno alumno) {
        return asistenciaRepository.findByAlumno(alumno);
    }

    @Transactional(readOnly = true)
    public List<Asistencia> findByAlumnoAndFecha(Alumno alumno, LocalDate fecha) {
        return asistenciaRepository.findByAlumnoAndFecha(alumno, fecha);
    }
    
    @Transactional(readOnly = true)
    public List<Asistencia> findByAlumnoIdAndFecha(Long alumnoId, LocalDate fecha) {
        return asistenciaRepository.findByAlumnoIdAndFecha(alumnoId, fecha);
    }
    
    @Transactional(readOnly = true)
    public List<Asistencia> findByFechaOrderByAlumnoNombre(LocalDate fecha) {
        return asistenciaRepository.findByFechaOrderByAlumnoNombreAsc(fecha);
    }

    @Transactional
    public Asistencia save(Asistencia asistencia) {
        Asistencia asistenciaAnterior = null;
        String accion = "CREAR";
        if (asistencia.getId() != null) {
            asistenciaAnterior = asistenciaRepository.findById(asistencia.getId()).orElse(null);
            accion = "ACTUALIZAR";
        }
        Asistencia asistenciaGuardada = asistenciaRepository.save(asistencia);
        auditoriaService.registrarAccion(accion, asistenciaGuardada, asistenciaAnterior);
        return asistenciaGuardada;
    }

    @Transactional
    public void deleteById(Long id) {
        Optional<Asistencia> asistenciaOpt = asistenciaRepository.findById(id);
        if (asistenciaOpt.isPresent()) {
            Asistencia asistencia = asistenciaOpt.get();
            asistenciaRepository.deleteById(id);
            auditoriaService.registrarAccion("ELIMINAR", asistencia, asistencia);
        }
    }
}