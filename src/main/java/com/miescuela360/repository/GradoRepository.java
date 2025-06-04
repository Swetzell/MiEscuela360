package com.miescuela360.repository;

import com.miescuela360.model.Grado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradoRepository extends JpaRepository<Grado, Long> {
    
    List<Grado> findByActivoTrue();
    
    Grado findByNombre(String nombre);
    
    boolean existsByNombre(String nombre);
}
