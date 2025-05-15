package com.miescuela360.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miescuela360.model.Alumno;
import com.miescuela360.model.Pago;
import com.miescuela360.model.Pago.EstadoPago;
import com.miescuela360.model.Pago.TipoPago;
import com.miescuela360.model.Usuario;
import com.miescuela360.repository.AlumnoRepository;
import com.miescuela360.repository.PagoRepository;
import com.miescuela360.repository.UsuarioRepository;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;
    
    @Autowired
    private AlumnoRepository alumnoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Obtiene todos los pagos registrados en el sistema
     */
    @Transactional(readOnly = true)
    public List<Pago> findAll() {
        return pagoRepository.findAll();
    }
    
    /**
     * Busca un pago por su ID
     */
    @Transactional(readOnly = true)
    public Optional<Pago> findById(Long id) {
        return pagoRepository.findById(id);
    }
    
    /**
     * Guarda un nuevo pago o actualiza uno existente
     */
    @Transactional
    public Pago save(Pago pago) {
        return pagoRepository.save(pago);
    }
    
    /**
     * Elimina un pago del sistema
     */
    @Transactional
    public void deleteById(Long id) {
        pagoRepository.deleteById(id);
    }
    
    /**
     * Procesa un pago pendiente como pagado
     */
    @Transactional
    public Pago procesarPago(Long id) {
        Optional<Pago> pagoOpt = pagoRepository.findById(id);
        if (pagoOpt.isPresent()) {
            Pago pago = pagoOpt.get();
            
            // Solo procesar si está pendiente
            if (pago.getEstado() == EstadoPago.PENDIENTE) {
                pago.setEstado(EstadoPago.PAGADO);
                pago.setFechaPago(LocalDate.now());
                return pagoRepository.save(pago);
            }
            return pago;
        }
        return null;
    }
    
    /**
     * Anula un pago
     */
    @Transactional
    public Pago anularPago(Long id) {
        Optional<Pago> pagoOpt = pagoRepository.findById(id);
        if (pagoOpt.isPresent()) {
            Pago pago = pagoOpt.get();
            
            // Solo anular si no está ya anulado
            if (pago.getEstado() != EstadoPago.ANULADO) {
                pago.setEstado(EstadoPago.ANULADO);
                return pagoRepository.save(pago);
            }
            return pago;
        }
        return null;
    }
    
    /**
     * Obtiene todos los pagos filtrados por estado
     */
    public List<Pago> obtenerPagosPorEstado(EstadoPago estado) {
        return pagoRepository.findByEstado(estado);
    }
    
    /**
     * Obtiene el total de pagos en el sistema
     */
    public long contarTotalPagos() {
        return pagoRepository.count();
    }
    
    /**
     * Obtiene el total de pagos por estado
     */
    public long contarPagosPorEstado(EstadoPago estado) {
        return pagoRepository.findByEstado(estado).size();
    }
    
    /**
     * Obtiene la suma total de montos pagados
     */
    public double obtenerMontoTotalPagado() {
        Double resultado = pagoRepository.sumMontoByEstado(EstadoPago.PAGADO);
        return resultado != null ? resultado : 0.0;
    }
    
    /**
     * Filtra pagos por rango de fechas y opcionalmente por estado
     */
    public List<Pago> filtrarPagos(LocalDate fechaInicio, LocalDate fechaFin, EstadoPago estado) {
        if (estado != null) {
            return pagoRepository.findByFechaBetweenAndEstado(fechaInicio, fechaFin, estado);
        } else {
            return pagoRepository.findByFechaBetween(fechaInicio, fechaFin);
        }
    }
    
    /**
     * Verifica y actualiza los pagos vencidos
     */
    @Transactional
    public int actualizarPagosVencidos() {
        LocalDate hoy = LocalDate.now();
        List<Pago> pagosVencidos = pagoRepository.findPagosVencidos(hoy);
        
        int contador = 0;
        for (Pago pago : pagosVencidos) {
            pago.setEstado(EstadoPago.VENCIDO);
            pagoRepository.save(pago);
            contador++;
        }
        
        return contador;
    }
    
    /**
     * Genera mensualidades para los alumnos seleccionados
     */
    @Transactional
    public List<Pago> generarMensualidades(List<Long> alumnoIds, int mes, int anio, int diasVencimiento, Long usuarioId) {
        List<Pago> pagosGenerados = new ArrayList<>();
        
        LocalDate fechaMes = LocalDate.of(anio, mes, 1);
        LocalDate fechaVencimiento = fechaMes.plusDays(diasVencimiento - 1);
        
        Optional<Usuario> usuarioOpt = usuarioId != null ? usuarioRepository.findById(usuarioId) : Optional.empty();
        
        for (Long alumnoId : alumnoIds) {
            Optional<Alumno> alumnoOpt = alumnoRepository.findById(alumnoId);
            if (alumnoOpt.isPresent()) {
                Alumno alumno = alumnoOpt.get();
                
                Long count = pagoRepository.countPagosByAlumnoAndTipoAndMesAndAnio(
                        alumno, TipoPago.PENSION, mes, anio);
                
                if (count == 0) {
                    Pago pago = new Pago();
                    pago.setAlumno(alumno);
                    pago.setTipo(TipoPago.PENSION);
                    pago.setMonto(BigDecimal.valueOf(alumno.getMontoMensual()));
                    pago.setFecha(LocalDate.now());
                    pago.setFechaVencimiento(fechaVencimiento);
                    pago.setEstado(EstadoPago.PENDIENTE);
                    
                    String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                     "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
                    String observacion = "Pensión del mes de " + meses[mes-1] + " " + anio;
                    pago.setObservaciones(observacion);
                    
                    usuarioOpt.ifPresent(usuario -> pago.setRegistradoPor(usuario));
                    
                    pagosGenerados.add(pagoRepository.save(pago));
                }
            }
        }
        
        return pagosGenerados;
    }
    
    /**
     * Obtiene los pagos de un alumno específico
     */
    @Transactional(readOnly = true)
    public List<Pago> findByAlumnoId(Long alumnoId) {
        return pagoRepository.findByAlumnoId(alumnoId);
    }
    
    /**
     * Obtiene la fecha del último pago de un alumno
     */
    public LocalDate obtenerFechaUltimoPago(Long alumnoId) {
        return pagoRepository.findUltimoPagoPorAlumno(alumnoId);
    }
    
    public List<EstadoPago> obtenerTodosLosEstados() {
        return Arrays.asList(EstadoPago.values());
    }
    
    public List<TipoPago> obtenerTodosLosTiposPago() {
        return Arrays.asList(TipoPago.values());
    }
      /**
     * Suma los montos por estado
     */
    public Double sumMontoByEstado(EstadoPago estado) {
        Double resultado = pagoRepository.sumMontoByEstado(estado);
        return resultado != null ? resultado : 0.0;
    }
}