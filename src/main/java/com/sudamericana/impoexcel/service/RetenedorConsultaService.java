package com.sudamericana.impoexcel.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudamericana.impoexcel.model.AgenteRetenedor;
import com.sudamericana.impoexcel.model.ConsultaRetenedor;
import com.sudamericana.impoexcel.repository.AgenteRetenedorRepository;
import com.sudamericana.impoexcel.repository.ConsultaRetenedorRepository;

import jakarta.annotation.PostConstruct;

@Service
public class RetenedorConsultaService {

    private static final Logger logger = LoggerFactory.getLogger(RetenedorConsultaService.class);
    private static final String API_RETENEDOR_URL = "https://api.migo.pe/api/v1/ruc/agentes-retencion";

    @Value("${api.key:ioDmB1FxJvrJkwrxioPwcrISiw41ZKqaOvMEBztjxmjG3VniHGZABPOlSa97}")
    private String apiKey;

    @Autowired
    private AgenteRetenedorRepository retenedorRepository;

    @Autowired
    private ConsultaRetenedorRepository consultaRepository;
    
    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void inicializar() {
        logger.info("Inicializando servicio de consulta de retenedores con API key: {}", apiKey.substring(0, 10) + "...");
    }

    @Autowired
private JdbcTemplate jdbcTemplate;

// Método para consultar los RUCs desde el SP
public List<String> obtenerRucsDesdeStoredProcedure() {
    logger.info("Obteniendo RUCs desde el stored procedure SP_leerRucClientes");
    try {
        List<String> rucs = jdbcTemplate.query(
            "EXEC SP_leerRucClientes",
            (rs, rowNum) -> rs.getString("ruccli").trim()
        );
        
        // Filtramos para asegurar que sean RUCs válidos
        List<String> rucsValidos = rucs.stream()
            .filter(ruc -> ruc != null && ruc.matches("\\d{11}"))
            .collect(Collectors.toList());
        
        logger.info("Se obtuvieron {} RUCs válidos del stored procedure", rucsValidos.size());
        return rucsValidos;
    } catch (Exception e) {
        logger.error("Error al obtener RUCs desde stored procedure: {}", e.getMessage(), e);
        throw new RuntimeException("Error al consultar RUCs de clientes: " + e.getMessage(), e);
    }
}

// Método para iniciar consulta masiva desde base de datos
@Async
public void consultarRetenedoresDesdeBD() {
    logger.info("Iniciando consulta masiva de retenedores desde la base de datos");
    try {
        List<String> rucs = obtenerRucsDesdeStoredProcedure();
        if (rucs.isEmpty()) {
            logger.warn("No se encontraron RUCs válidos en la base de datos");
            return;
        }
        
        logger.info("Procesando {} RUCs de clientes", rucs.size());
        
        // Procesar cada RUC individualmente con un delay entre solicitudes
        int procesados = 0;
        for (String ruc : rucs) {
            try {
                logger.info("Consultando RUC {} ({} de {})", ruc, ++procesados, rucs.size());
                consultarRetenedor(ruc);
                
                // Añadir un pequeño delay para no sobrecargar la API
                Thread.sleep(1500); // 1.5 segundos entre solicitudes
            } catch (Exception e) {
                logger.error("Error al procesar RUC {}: {}", ruc, e.getMessage());
                // Continuar con el siguiente RUC a pesar del error
            }
        }
        
        logger.info("Consulta masiva finalizada. Se procesaron {} RUCs", procesados);
    } catch (Exception e) {
        logger.error("Error en la consulta masiva desde BD: {}", e.getMessage(), e);
    }
}


    /**
     * Consulta si un RUC es agente retenedor
     */
    @Transactional
    public AgenteRetenedor consultarRetenedor(String ruc) {
        logger.info("Iniciando consulta de retenedor para RUC: {}", ruc);

        // Validar formato del RUC
        if (ruc == null || ruc.trim().length() != 11 || !ruc.matches("\\d+")) {
            logger.warn("RUC inválido: {}", ruc);
            registrarConsulta(ruc, false, "RUC inválido: debe tener 11 dígitos numéricos", null);
            throw new IllegalArgumentException("El RUC debe tener 11 dígitos numéricos");
        }

        // Verificar si ya existe en la base de datos
        AgenteRetenedor retenedorExistente = retenedorRepository.findByRuc(ruc);
        if (retenedorExistente != null) {
            logger.info("El RUC {} ya existe en la base de datos", ruc);
            registrarConsulta(ruc, true, "Recuperado de base de datos", true);
            return retenedorExistente;
        }

        // Si no existe, consultarlo en la API
        try {
            JsonNode datosRetenedor = consultarRetenedorApi(ruc);
            
            if (datosRetenedor == null) {
                logger.warn("No se pudo consultar si el RUC {} es agente retenedor", ruc);
                registrarConsulta(ruc, false, "Error al consultar si es agente retenedor", null);
                return null;
            }
            
            boolean esRetenedor = datosRetenedor.has("success") && datosRetenedor.get("success").asBoolean();
            
            // Si no es retenedor, registramos la consulta ysistema da null
            if (!esRetenedor) {
                String mensaje = datosRetenedor.has("message") ? 
                        datosRetenedor.get("message").asText() : "No es agente retenedor";
                logger.info("El RUC {} no es agente retenedor: {}", ruc, mensaje);
                registrarConsulta(ruc, true, mensaje, false);
                return null;
            }
            
            // Es agente retenedor, guardamos la información
            AgenteRetenedor nuevoRetenedor = new AgenteRetenedor();
            nuevoRetenedor.setRuc(ruc);
            nuevoRetenedor.setRazonSocial(datosRetenedor.get("nombre_o_razon_social").asText());
            
            // Parsear fecha "a_partir_del"
            if (datosRetenedor.has("a_partir_del") && !datosRetenedor.get("a_partir_del").isNull()) {
                String fechaStr = datosRetenedor.get("a_partir_del").asText();
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    nuevoRetenedor.setAPartirDel(LocalDate.parse(fechaStr, formatter));
                } catch (Exception e) {
                    logger.warn("Error al parsear fecha 'a_partir_del': {}", e.getMessage());
                }
            }
            
            if (datosRetenedor.has("resolucion")) {
                nuevoRetenedor.setResolucion(datosRetenedor.get("resolucion").asText());
            }
            
            nuevoRetenedor.setFechaConsulta(LocalDateTime.now());
            
            AgenteRetenedor retenedorGuardado = retenedorRepository.save(nuevoRetenedor);
            logger.info("Agente Retenedor con RUC {} guardado exitosamente", ruc);
            
            registrarConsulta(ruc, true, "Es agente retenedor", true);
            
            return retenedorGuardado;
        } catch (Exception e) {
            logger.error("Error al consultar RUC {}: {}", ruc, e.getMessage(), e);
            registrarConsulta(ruc, false, "Error: " + e.getMessage(), null);
            throw e;
        }
    }

    private JsonNode consultarRetenedorApi(String ruc) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Crear el cuerpo de la petición
            ObjectMapper mapper = new ObjectMapper();
            JsonNode requestBody = mapper.createObjectNode()
                .put("token", apiKey)
                .put("ruc", ruc);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                API_RETENEDOR_URL, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return mapper.readTree(response.getBody());
            } else {
                logger.warn("Error al consultar retenedor {}: Código de estado {}", ruc, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error al consultar API de retenedores para {}: {}", ruc, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Consulta varios RUCs de forma asíncrona
     */
    @Async
    public void consultarVariosRucs(List<String> listaRucs) {
        logger.info("Iniciando consulta masiva de {} RUCs", listaRucs.size());
        
        int procesados = 0;
        int exitosos = 0;
        int fallidos = 0;
        
        for (String ruc : listaRucs) {
            try {
                logger.info("Consultando RUC {} ({} de {})", ruc, ++procesados, listaRucs.size());
                AgenteRetenedor retenedor = consultarRetenedor(ruc);
                
                if (retenedor != null) {
                    exitosos++;
                } else {
                    // No es retenedor, pero la consulta fue exitosa
                }
                
                // Añadir un pequeño delay para no sobrecargar la API
                Thread.sleep(1500); // 1.5 segundos entre solicitudes
            } catch (Exception e) {
                logger.error("Error al consultar RUC {}: {}", ruc, e.getMessage());
                registrarConsulta(ruc, false, "Error: " + e.getMessage(), null);
                fallidos++;
            }
        }
        
        logger.info("Consulta masiva finalizada. Total: {}, Exitosos: {}, Fallidos: {}", 
                listaRucs.size(), exitosos, fallidos);
    }
    
    /**
     * Registra una consulta en la base de datos
     */
    private void registrarConsulta(String ruc, boolean exitoso, String mensaje, Boolean esRetenedor) {
        try {
            ConsultaRetenedor consulta = new ConsultaRetenedor();
            consulta.setRuc(ruc);
            consulta.setFechaConsulta(LocalDateTime.now());
            consulta.setExitoso(exitoso);
            consulta.setMensaje(mensaje);
            consulta.setEsRetenedor(esRetenedor);
            
            consultaRepository.save(consulta);
            logger.info("Consulta registrada: RUC {}, Exitoso: {}, Mensaje: {}, Es retenedor: {}", 
                    ruc, exitoso, mensaje, esRetenedor);
        } catch (Exception e) {
            logger.error("Error al registrar consulta: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene los retenedores más recientes
     */
    public List<AgenteRetenedor> obtenerRetenedoresRecientes(int limite) {
        return retenedorRepository.findAllOrderByFechaConsultaDesc(PageRequest.of(0, limite))
                .getContent();
    }
    
    /**
     * Obtiene el historial de consultas recientes
     */
    public List<ConsultaRetenedor> obtenerHistorialReciente(int limite) {
        return consultaRepository.findAllOrderByFechaConsultaDesc(PageRequest.of(0, limite))
                .getContent();
    }
    
    /**
     * Obtiene el historial de consultas para un RUC específico
     */
    public List<ConsultaRetenedor> obtenerHistorialPorRuc(String ruc) {
        return consultaRepository.findByRucOrderByFechaConsultaDesc(ruc);
    }
    
    /**
     * Verifica si un RUC es agente retenedor
     */
    public boolean esRetenedor(String ruc) {
        return retenedorRepository.findByRuc(ruc) != null;
    }

    public AgenteRetenedor obtenerRetenedorPorRuc(String ruc) {
        return retenedorRepository.findByRuc(ruc);
    }
}