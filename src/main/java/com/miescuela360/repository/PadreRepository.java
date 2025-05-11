package com.miescuela360.repository;

import com.miescuela360.model.Padre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PadreRepository extends JpaRepository<Padre, Long> {
    Optional<Padre> findByDni(String dni);
    boolean existsByDni(String dni);
} 