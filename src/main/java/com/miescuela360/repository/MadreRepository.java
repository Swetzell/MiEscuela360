package com.miescuela360.repository;

import com.miescuela360.model.Madre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MadreRepository extends JpaRepository<Madre, Long> {
    Optional<Madre> findByDni(String dni);
    boolean existsByDni(String dni);
} 