package com.miescuela360.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Service
public class DniApiService {
    @Value("${api.peru.token}")
    private String apiToken;

    // URL de la API de apiperu.dev
    private final String API_URL = "https://apiperu.dev/api/dni";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public DniApiService(@org.springframework.beans.factory.annotation.Qualifier("apiRestTemplate") RestTemplate restTemplate, 
                         @org.springframework.beans.factory.annotation.Qualifier("objectMapperWithJavaTime") ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Obtiene el valor del token configurado para la API Peru.
     * @return El valor actual del token o null si no está configurado
     */
    public String getApiToken() {
        return apiToken;
    }

    public DniResponse consultarDni(String dni) {
        try {
            // Crear el cuerpo de la solicitud
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("dni", dni);
            
            // Log de depuración
            System.out.println("Consultando DNI en apiperu.dev: " + dni);
            System.out.println("URL: " + API_URL);
            
            // Realizar la petición
            ResponseEntity<String> response = restTemplate.exchange(
                API_URL, 
                HttpMethod.POST, 
                new HttpEntity<>(requestBody), // Los headers ahora se manejan en el RestTemplate
                String.class
            );
            
            // Procesar la respuesta exitosa
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                
                // Verificar si la respuesta contiene datos válidos
                if (rootNode.has("success") && rootNode.get("success").asBoolean() && rootNode.has("data")) {
                    JsonNode dataNode = rootNode.get("data");
                    String nombreCompleto = dataNode.has("nombres") ? dataNode.get("nombres").asText() : "";
                    String apellidoPaterno = dataNode.has("apellido_paterno") ? dataNode.get("apellido_paterno").asText() : "";
                    String apellidoMaterno = dataNode.has("apellido_materno") ? dataNode.get("apellido_materno").asText() : "";
                    
                    // Construir el nombre completo
                    String nombreCompletoStr = String.format("%s %s %s", 
                        nombreCompleto, 
                        apellidoPaterno, 
                        apellidoMaterno).trim();
                    
                    return new DniResponse(true, dni, nombreCompletoStr);
                } else {
                    String errorMessage = rootNode.has("message") ? 
                        rootNode.get("message").asText() : "Error desconocido al consultar el DNI";
                    return new DniResponse(false, dni, errorMessage);
                }
            } else {
                return new DniResponse(false, dni, "Error en la respuesta del servidor");
            }
            
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            String errorMessage = "Error en la API: ";
            
            // Intentar extraer el mensaje de error del cuerpo de la respuesta
            try {
                if (ex.getResponseBodyAsString() != null && !ex.getResponseBodyAsString().isEmpty()) {
                    JsonNode errorNode = objectMapper.readTree(ex.getResponseBodyAsString());
                    if (errorNode.has("message")) {
                        errorMessage += errorNode.get("message").asText();
                    } else {
                        errorMessage += ex.getStatusText();
                    }
                } else {
                    errorMessage += ex.getStatusText();
                }
            } catch (Exception e) {
                errorMessage += ex.getStatusText();
            }
            
            return new DniResponse(false, dni, errorMessage);
            
        } catch (Exception e) {
            System.out.println("Error general al consultar DNI: " + e.getMessage());
            e.printStackTrace();
            return new DniResponse(false, dni, "Error al procesar la solicitud: " + e.getMessage());
        }
    }

    public static class DniResponse {
        private boolean success;
        private String dni;
        private String nombre;

        public DniResponse() {
            // Constructor vacío necesario para deserialización
        }

        public DniResponse(boolean success, String dni, String nombre) {
            this.success = success;
            this.dni = dni;
            this.nombre = nombre;
        }

        // Getters y setters deben coincidir exactamente con los nombres de campos de la API
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
    }
}
