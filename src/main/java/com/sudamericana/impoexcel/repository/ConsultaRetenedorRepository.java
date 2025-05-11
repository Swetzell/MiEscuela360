package com.sudamericana.impoexcel.repository;

import com.sudamericana.impoexcel.model.ConsultaRetenedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsultaRetenedorRepository extends JpaRepository<ConsultaRetenedor, Long> {
    List<ConsultaRetenedor> findByRucOrderByFechaConsultaDesc(String ruc);
    
    @Query("SELECT c FROM ConsultaRetenedor c ORDER BY c.fechaConsulta DESC")
    Page<ConsultaRetenedor> findAllOrderByFechaConsultaDesc(Pageable pageable);
}