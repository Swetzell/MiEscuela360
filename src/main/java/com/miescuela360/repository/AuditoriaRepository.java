package com.miescuela360.repository;

import com.miescuela360.model.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    @Query("SELECT DISTINCT a.entidad FROM Auditoria a")
    List<String> findDistinctEntidades();
    
    @Query("SELECT DISTINCT a.accion FROM Auditoria a")
    List<String> findDistinctAcciones();
    
    Page<Auditoria> findByEntidad(String entidad, Pageable pageable);
    
    Page<Auditoria> findByEntidadAndAccion(String entidad, String accion, Pageable pageable);

    Page<Auditoria> findByAccion(String accion, Pageable pageable);
}