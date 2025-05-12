package com.miescuela360.repository;

import com.miescuela360.model.Asistencia;
import com.miescuela360.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    List<Asistencia> findByFecha(LocalDate fecha);
    List<Asistencia> findByAlumno(Alumno alumno);
    List<Asistencia> findByAlumnoAndFecha(Alumno alumno, LocalDate fecha);
    List<Asistencia> findByAlumnoIdAndFecha(Long alumnoId, LocalDate fecha);
    List<Asistencia> findByFechaOrderByAlumnoNombreAsc(LocalDate fecha);
}