package com.miescuela360.repository;

import com.miescuela360.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    Optional<Alumno> findByDni(String dni);
    boolean existsByDni(String dni);
    List<Alumno> findByGradoActualAndSeccionActual(String grado, String seccion);
    List<Alumno> findByGradoIdAndSeccionIdAndActivo(Long gradoId, Long seccionId, boolean activo);
    List<Alumno> findByGradoIdAndSeccionId(Long gradoId, Long seccionId);
    
    List<Alumno> findByGradoIdAndActivo(Long gradoId, boolean activo);
    List<Alumno> findByGradoId(Long gradoId);
    
    List<Alumno> findBySeccionIdAndActivo(Long seccionId, boolean activo);
    List<Alumno> findBySeccionId(Long seccionId);
    
    List<Alumno> findByActivo(boolean activo);
} 