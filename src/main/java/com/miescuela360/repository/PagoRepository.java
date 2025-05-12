package com.miescuela360.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.miescuela360.model.Alumno;
import com.miescuela360.model.Pago;
import com.miescuela360.model.Pago.EstadoPago;
import com.miescuela360.model.Pago.TipoPago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    
    /**
     * Busca pagos por alumno ordenados por fecha descendente
     */
    List<Pago> findByAlumnoOrderByFechaDesc(Alumno alumno);
    
    /**
     * Busca pagos por alumno ID ordenados por fecha descendente
     */
    List<Pago> findByAlumnoIdOrderByFechaDesc(Long alumnoId);
    
    /**
     * Busca pagos por estado
     */
    List<Pago> findByEstado(EstadoPago estado);
    
    /**
     * Busca pagos entre dos fechas
     */
    List<Pago> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    /**
     * Busca pagos entre dos fechas y con un estado específico
     */
    List<Pago> findByFechaBetweenAndEstado(LocalDate fechaInicio, LocalDate fechaFin, EstadoPago estado);
    
    /**
     * Busca pagos por alumno
     */
    List<Pago> findByAlumno(Alumno alumno);
    
    /**
     * Cuenta los pagos de un tipo específico para un alumno en un mes y año específicos
     */
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.alumno = :alumno AND p.tipo = :tipo " +
           "AND MONTH(p.fecha) = :mes AND YEAR(p.fecha) = :anio")
    Long countPagosByAlumnoAndTipoAndMesAndAnio(
            @Param("alumno") Alumno alumno, 
            @Param("tipo") TipoPago tipo, 
            @Param("mes") int mes, 
            @Param("anio") int anio);
    
    /**
     * Obtiene la fecha del último pago de un alumno
     */
    @Query("SELECT MAX(p.fechaPago) FROM Pago p WHERE p.alumno.id = :alumnoId AND p.estado = 'PAGADO'")
    LocalDate findUltimoPagoPorAlumno(@Param("alumnoId") Long alumnoId);
    
    /**
     * Encuentra pagos vencidos (con fecha de vencimiento anterior a la fecha actual y estado PENDIENTE)
     */
    @Query("SELECT p FROM Pago p WHERE p.fechaVencimiento < :fechaActual AND p.estado = 'PENDIENTE'")
    List<Pago> findPagosVencidos(@Param("fechaActual") LocalDate fechaActual);
    
    /**
     * Calcula la suma de montos por estado
     */
    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.estado = :estado")
    Double sumMontoByEstado(@Param("estado") EstadoPago estado);
    
    /**
     * Busca pagos por tipo
     */
    List<Pago> findByTipo(TipoPago tipo);
    
    /**
     * Busca pagos por alumno y estado
     */
    List<Pago> findByAlumnoAndEstado(Alumno alumno, EstadoPago estado);
    
    /**
     * Busca pagos por alumno, tipo y estado
     */
    List<Pago> findByAlumnoAndTipoAndEstado(Alumno alumno, TipoPago tipo, EstadoPago estado);
    
    /**
     * Encuentra pagos pendientes con fecha de vencimiento próxima
     */
    @Query("SELECT p FROM Pago p WHERE p.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin AND p.estado = 'PENDIENTE'")
    List<Pago> findPagosPendientesProximosAVencer(
            @Param("fechaInicio") LocalDate fechaInicio, 
            @Param("fechaFin") LocalDate fechaFin);

    List<Pago> findByAlumnoId(Long alumnoId);
}