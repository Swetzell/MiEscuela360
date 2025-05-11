package com.sudamericana.impoexcel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sudamericana.impoexcel.model.ConsultaSunat;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface ConsultaSunatRepository extends JpaRepository<ConsultaSunat, Long> {
    List<ConsultaSunat> findByRucOrderByFechaConsultaDesc(String ruc);

    @Query("SELECT c FROM ConsultaSunat c ORDER BY c.fechaConsulta DESC")
    Page<ConsultaSunat> findAllOrderByFechaConsultaDesc(Pageable pageable);

}