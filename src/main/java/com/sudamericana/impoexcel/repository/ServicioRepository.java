package com.sudamericana.impoexcel.repository;

import com.sudamericana.impoexcel.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    Optional<Servicio> findByNombre(String nombre);
    Optional<Servicio> findByRuta(String ruta);
    List<Servicio> findByRutaStartingWith(String rutaPrefix);
}