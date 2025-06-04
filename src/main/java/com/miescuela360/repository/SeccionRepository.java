package com.miescuela360.repository;

import com.miescuela360.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeccionRepository extends JpaRepository<Seccion, Long> {
    
    List<Seccion> findByActivoTrue();
    
    Seccion findByNombre(String nombre);
    
    boolean existsByNombre(String nombre);
}
