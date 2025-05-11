package com.sudamericana.impoexcel.service;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.sudamericana.impoexcel.model.ClienteSunat;
import com.sudamericana.impoexcel.model.ConsultaSunat;
import com.sudamericana.impoexcel.repository.ClienteSunatRepository;
import com.sudamericana.impoexcel.repository.ConsultaSunatRepository;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class SunatConsultaService {

    private static final Logger logger = LoggerFactory.getLogger(SunatConsultaService.class);
    private static final String API_RUC_URL = "https://api.migo.pe/api/v1/ruc";
    private static final String API_RUC_MASIVA_URL = "https://api.migo.pe/api/v1/ruc/collection";

    @Value("${api.key:ioDmB1FxJvrJkwrxioPwcrISiw41ZKqaOvMEBztjxmjG3VniHGZABPOlSa97}")
    private String apiKey;

    @Autowired
    private ClienteSunatRepository clienteRepository;

    @Autowired
    private ConsultaSunatRepository consultaRepository;
    
    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void inicializar() {
        logger.info("Servicio de consulta SUNAT API inicializado con API key: {}", 
                apiKey.substring(0, 10) + "...");
    }

    /**
     * Consulta un RUC específico en la API y guarda la información
     */
    @Transactional
    public ClienteSunat consultarRuc(String ruc) {
        logger.info("Iniciando consulta para RUC: {}", ruc);

        // Validar formato del RUC
        if (ruc == null || ruc.trim().length() != 11 || !ruc.matches("\\d+")) {
            logger.warn("RUC inválido: {}", ruc);
            registrarConsulta(ruc, false, "RUC inválido: debe tener 11 dígitos numéricos");
            throw new IllegalArgumentException("El RUC debe tener 11 dígitos numéricos");
        }

        // Verificar si ya existe en la base de datos
        ClienteSunat clienteExistente = clienteRepository.findByRuc(ruc);
        if (clienteExistente != null) {
            logger.info("El RUC {} ya existe en la base de datos", ruc);
            registrarConsulta(ruc, true, "Recuperado de base de datos");
            return clienteExistente;
        }

        // Si no existe, consultar en la API
        try {
            JsonNode datosRuc = consultarRucEnApi(ruc);

            if (datosRuc == null || !datosRuc.has("success") || !datosRuc.get("success").asBoolean()) {
                String mensaje = datosRuc != null && datosRuc.has("message") ? 
                        datosRuc.get("message").asText() : "No se encontraron datos en la API";
                logger.warn("No se encontraron datos para el RUC {} en la API: {}", ruc, mensaje);
                registrarConsulta(ruc, false, mensaje);
                return null;
            }

            // Crear y guardar el nuevo cliente
            ClienteSunat nuevoCliente = new ClienteSunat();
            nuevoCliente.setRuc(ruc);
            nuevoCliente.setRazonSocial(datosRuc.has("nombre_o_razon_social") ? 
                    datosRuc.get("nombre_o_razon_social").asText() : "");
            nuevoCliente.setEstado(datosRuc.has("estado_del_contribuyente") ? 
                    datosRuc.get("estado_del_contribuyente").asText() : "");
            nuevoCliente.setCondicion(datosRuc.has("condicion_de_domicilio") ? 
                    datosRuc.get("condicion_de_domicilio").asText() : "");
            nuevoCliente.setDireccion(datosRuc.has("direccion") ? 
                    datosRuc.get("direccion").asText() : "");
            nuevoCliente.setUbigeo(datosRuc.has("ubigeo") ? 
                    datosRuc.get("ubigeo").asText() : "");
            
            // Convertir la fecha de actualización
            if (datosRuc.has("actualizado_en")) {
                String fechaStr = datosRuc.get("actualizado_en").asText();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime fechaActualizacion = LocalDateTime.parse(fechaStr, formatter);
                nuevoCliente.setUltimaActualizacion(fechaActualizacion);
            }
            
            nuevoCliente.setFechaConsulta(LocalDateTime.now());

            ClienteSunat clienteGuardado = clienteRepository.save(nuevoCliente);
            logger.info("Cliente con RUC {} guardado exitosamente", ruc);

            // Registrar la consulta
            registrarConsulta(ruc, true, "Datos obtenidos y guardados correctamente");

            return clienteGuardado;
        } catch (Exception e) {
            logger.error("Error al consultar RUC {}: {}", ruc, e.getMessage(), e);
            registrarConsulta(ruc, false, "Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Consulta varios RUCs de consulta masiva
     * @param listaRucs Lista de RUCs a consultar
     */
    @Async
    public void consultarVariosRucs(List<String> listaRucs) {
        logger.info("Iniciando consulta masiva de {} RUCs", listaRucs.size());
        
        try {
            // Si son pocos RUCs, usar consulta individual por ser más confiable
            if (listaRucs.size() <= 10) {
                for (String ruc : listaRucs) {
                    try {
                        consultarRuc(ruc);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        logger.error("Error al consultar RUC {}: {}", ruc, e.getMessage());
                        registrarConsulta(ruc, false, "Error: " + e.getMessage());
                    }
                }
            } else {
                // Para muchos RUCs, usar la API de consulta masiva
                JsonNode resultadoMasivo = consultarRucsEnMasa(listaRucs);
                
                if (resultadoMasivo != null && resultadoMasivo.has("data") && resultadoMasivo.get("data").isArray()) {
                    ArrayNode resultados = (ArrayNode) resultadoMasivo.get("data");
                    
                    for (JsonNode resultado : resultados) {
                        try {
                            if (resultado.has("ruc")) {
                                String ruc = resultado.get("ruc").asText();
                                
                                // Verificar si ya existe en la base de datos
                                ClienteSunat clienteExistente = clienteRepository.findByRuc(ruc);
                                if (clienteExistente != null) {
                                    logger.info("El RUC {} ya existe en la base de datos", ruc);
                                    registrarConsulta(ruc, true, "Recuperado de base de datos");
                                    continue;
                                }
                                
                                // Crear nuevo cliente
                                ClienteSunat nuevoCliente = new ClienteSunat();
                                nuevoCliente.setRuc(ruc);
                                nuevoCliente.setRazonSocial(resultado.has("razon_social") ? 
                                        resultado.get("razon_social").asText() : "");
                                nuevoCliente.setNombreComercial(resultado.has("nombre_comercial") ? 
                                        resultado.get("nombre_comercial").asText() : "");
                                nuevoCliente.setEstado(resultado.has("estado") ? 
                                        resultado.get("estado").asText() : "");
                                nuevoCliente.setCondicion(resultado.has("condicion") ? 
                                        resultado.get("condicion").asText() : "");
                                
                                // Dirección
                                if (resultado.has("direccion")) {
                                    nuevoCliente.setDireccion(resultado.get("direccion").asText());
                                } else if (resultado.has("direccion_completa")) {
                                    nuevoCliente.setDireccion(resultado.get("direccion_completa").asText());
                                }
                                
                                // Ubigeo
                                StringBuilder ubigeo = new StringBuilder();
                                if (resultado.has("departamento")) ubigeo.append(resultado.get("departamento").asText());
                                if (resultado.has("provincia")) {
                                    if (ubigeo.length() > 0) ubigeo.append(" - ");
                                    ubigeo.append(resultado.get("provincia").asText());
                                }
                                if (resultado.has("distrito")) {
                                    if (ubigeo.length() > 0) ubigeo.append(" - ");
                                    ubigeo.append(resultado.get("distrito").asText());
                                }
                                if (ubigeo.length() > 0) {
                                    nuevoCliente.setUbigeo(ubigeo.toString());
                                }
                                
                                // Tipo contribuyente
                                if (resultado.has("tipo_contribuyente")) {
                                    nuevoCliente.setTipoContribuyente(resultado.get("tipo_contribuyente").asText());
                                } else if (resultado.has("tipo")) {
                                    nuevoCliente.setTipoContribuyente(resultado.get("tipo").asText());
                                }
                                
                                nuevoCliente.setFechaConsulta(LocalDateTime.now());
                                
                                clienteRepository.save(nuevoCliente);
                                logger.info("Cliente con RUC {} guardado exitosamente desde consulta masiva", ruc);
                                
                                registrarConsulta(ruc, true, "Datos obtenidos y guardados correctamente desde consulta masiva");
                            }
                        } catch (Exception e) {
                            logger.error("Error al procesar resultado masivo: {}", e.getMessage(), e);
                            if (resultado.has("ruc")) {
                                registrarConsulta(resultado.get("ruc").asText(), false, "Error: " + e.getMessage());
                            }
                        }
                    }
                } else {
                    // Si falló la consulta masiva, intentar individualmente
                    logger.warn("La consulta masiva falló, intentando consultas individuales");
                    for (String ruc : listaRucs) {
                        try {
                            consultarRuc(ruc);
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            logger.error("Error al consultar RUC {}: {}", ruc, e.getMessage());
                            registrarConsulta(ruc, false, "Error: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error en la consulta masiva: {}", e.getMessage(), e);
        }
        
        logger.info("Consulta masiva finalizada");
    }

    /**
     * Consulta información de un RUC en la API
     */
    private JsonNode consultarRucEnApi(String ruc) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Crear petición
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("token", apiKey);
            requestBody.put("ruc", ruc);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                API_RUC_URL, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null && responseBody.trim().startsWith("{")) {
                    return mapper.readTree(responseBody);
                } else {
                    logger.warn("La respuesta de la API no es un JSON válido: {}", responseBody);
                    return null;
                }
            } else {
                logger.warn("Error al consultar RUC {}: Código de estado {}", ruc, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error al consultar API para RUC {}: {}", ruc, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Consulta múltiples RUCs mediante la API de consulta masiva
     */
    private JsonNode consultarRucsEnMasa(List<String> rucs) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Crear el cuerpo JSON con los RUCs
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode requestBody = mapper.createObjectNode();
            ArrayNode rucsArray = mapper.createArrayNode();
            
            for (String ruc : rucs) {
                rucsArray.add(ruc);
            }
            
            requestBody.set("rucs", rucsArray);
            
            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                API_RUC_MASIVA_URL, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return mapper.readTree(response.getBody());
            } else {
                logger.warn("Error al consultar RUCs en masa: Código de estado {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error al consultar API masiva: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Registra una consulta en la base de datos
     */
    private void registrarConsulta(String ruc, boolean exitoso, String mensaje) {
        try {
            ConsultaSunat consulta = new ConsultaSunat();
            consulta.setRuc(ruc);
            consulta.setFechaConsulta(LocalDateTime.now());
            consulta.setExitoso(exitoso);
            consulta.setMensaje(mensaje);
            
            consultaRepository.save(consulta);
            logger.info("Consulta registrada: RUC {}, Exitoso: {}, Mensaje: {}", ruc, exitoso, mensaje);
        } catch (Exception e) {
            logger.error("Error al registrar consulta: {}", e.getMessage(), e);
        }
    }
    
    public List<ClienteSunat> obtenerClientesRecientes(int limite) {
        return clienteRepository.findAll().stream()
                .sorted(Comparator.comparing(ClienteSunat::getFechaConsulta).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }
    
    public List<ConsultaSunat> obtenerHistorialReciente(int limite) {
        return consultaRepository.findAll().stream()
                .sorted(Comparator.comparing(ConsultaSunat::getFechaConsulta).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    public List<ConsultaSunat> obtenerHistorialPorRuc(String ruc) {
        return consultaRepository.findByRucOrderByFechaConsultaDesc(ruc);
    }

    public boolean verificarConexionBD() {
        try {
            // Intentamos hacer una operación simple en la base de datos
            long count = clienteRepository.count();
            logger.info("Conexión a la base de datos correcta. Número de clientes: {}", count);
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar la conexión a la base de datos", e);
            return false;
        }
    }
}