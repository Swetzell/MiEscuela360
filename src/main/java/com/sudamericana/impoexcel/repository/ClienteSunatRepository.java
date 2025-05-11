package com.sudamericana.impoexcel.repository;

import com.sudamericana.impoexcel.model.ClienteSunat;
import com.sudamericana.impoexcel.model.ConsultaSunat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ClienteSunatRepository extends JpaRepository<ClienteSunat, String> {
    ClienteSunat findByRuc(String ruc);

    @Query("SELECT c FROM ClienteSunat c ORDER BY c.fechaConsulta DESC")
    Page<ClienteSunat> findAllOrderByFechaConsultaDesc(Pageable pageable);

}