package com.miescuela360.repository;

import com.miescuela360.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    Optional<Servicio> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
} 