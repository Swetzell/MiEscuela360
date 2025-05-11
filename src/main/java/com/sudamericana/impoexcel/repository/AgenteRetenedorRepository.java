package com.sudamericana.impoexcel.repository;
import com.sudamericana.impoexcel.model.AgenteRetenedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AgenteRetenedorRepository extends JpaRepository<AgenteRetenedor, String> {
    AgenteRetenedor findByRuc(String ruc);
    
    @Query("SELECT a FROM AgenteRetenedor a ORDER BY a.fechaConsulta DESC")
    Page<AgenteRetenedor> findAllOrderByFechaConsultaDesc(Pageable pageable);
}