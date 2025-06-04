package com.miescuela360.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class DniApiService {
    @Value("${api.migo.token}")
    private String apiToken;

    // Actualizar si cambia la URL de la API
    private final String API_URL = "https://api.migo.pe/api/v1/dni";
    
    // Flag para marcar cuando el token se detecta como inválido
    private boolean tokenFailureDetected = false;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Inyectamos el bean específico para APIs
    public DniApiService(@org.springframework.beans.factory.annotation.Qualifier("apiRestTemplate") RestTemplate restTemplate, 
                         @org.springframework.beans.factory.annotation.Qualifier("objectMapperWithJavaTime") ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Obtiene el valor del token configurado para la API MiGo.
     * Este método se usa principalmente para diagnósticos.
     * 
     * @return El valor actual del token o null si no está configurado
     */
    public String getApiToken() {
        return apiToken;
    }
    
    /**
     * Verifica si se ha detectado un fallo de token en llamadas anteriores.
     * Esto es útil para determinar si el problema es con el token.
     * 
     * @return true si se detectó un problema con el token, false en caso contrario
     */
    public boolean isTokenFailureDetected() {
        return tokenFailureDetected;
    }

    public DniResponse consultarDni(String dni) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Asegurémonos de aceptar respuestas JSON
            headers.set("Accept", "application/json");
            
            // Añadir encabezados adicionales que podrían ser necesarios
            headers.set("User-Agent", "MiEscuela360-JavaClient");
            
            // Creamos el mapa exactamente como lo espera la API
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("token", apiToken);
            requestMap.put("dni", dni);

            // Log para verificar el token y DNI (oscureciendo parte del token por seguridad)
            String tokenMasked = apiToken.length() > 8 ? 
                apiToken.substring(0, 4) + "..." + apiToken.substring(apiToken.length() - 4) : apiToken;
            System.out.println("Token usado (parcial): " + tokenMasked);
            System.out.println("DNI consultado: " + dni);
            
            // Creamos la entidad de la solicitud
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestMap, headers);
            System.out.println("Enviando solicitud a: " + API_URL);
            
            try {
                System.out.println("Request completo:");
                System.out.println("URL: " + API_URL);
                System.out.println("Headers: " + headers);
                System.out.println("Body: " + objectMapper.writeValueAsString(requestMap));
                
                ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);
                
                System.out.println("Respuesta HTTP: " + response.getStatusCode());
                System.out.println("Headers de respuesta: " + response.getHeaders());
                System.out.println("Cuerpo de respuesta: " + response.getBody());
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    DniResponse dniResponse = objectMapper.readValue(response.getBody(), DniResponse.class);
                    System.out.println("Respuesta parseada: success=" + dniResponse.isSuccess() + ", nombre=" + dniResponse.getNombre());
                    return dniResponse;
                } else {
                    System.out.println("La respuesta no fue exitosa o el cuerpo está vacío");
                    return new DniResponse(false, dni, null);
                }
            } catch (org.springframework.web.client.HttpClientErrorException ex) {
                System.out.println("Error HTTP: " + ex.getStatusCode());
                System.out.println("Headers de error: " + ex.getResponseHeaders());
                System.out.println("Mensaje de error: " + ex.getResponseBodyAsString());
                System.out.println("¿Tiene cuerpo la respuesta?: " + (ex.getResponseBodyAsString() != null && !ex.getResponseBodyAsString().isEmpty()));
                
                // Manejo específico para cada tipo de error HTTP
                switch (ex.getStatusCode().value()) {
                    case 400:
                        System.out.println("Error 400 Bad Request: Los parámetros enviados no son correctos");
                        return new DniResponse(false, dni, "Los parámetros enviados a la API no son correctos. DNI: " + dni);
                        
                    case 401:
                        System.out.println("Error 401 Unauthorized: Autenticación fallida");
                        return new DniResponse(false, dni, "Error de autenticación con la API. El token no fue aceptado.");
                        
                    case 403:
                        // Marcar el token como inválido para registro interno
                        tokenFailureDetected = true;
                        
                        String errorMsg = "Error de acceso (403): El token API MiGo parece ser inválido o estar expirado. Contacte al administrador para actualizar el token.";
                        System.out.println(errorMsg);
                        System.out.println("IMPORTANTE: Es necesario actualizar el token MiGo en application.properties");
                        
                        try {
                            // Intentar ver si hay un mensaje específico en el cuerpo de la respuesta
                            if (ex.getResponseBodyAsString() != null && !ex.getResponseBodyAsString().isEmpty()) {
                                Map<String, Object> errorResponse = objectMapper.readValue(ex.getResponseBodyAsString(),
                                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                                if (errorResponse.containsKey("message")) {
                                    errorMsg += " Mensaje del servidor: " + errorResponse.get("message");
                                    System.out.println("Mensaje de error del servidor: " + errorResponse.get("message"));
                                }
                            }
                        } catch (Exception e) {
                            errorMsg += " (No se pudo parsear la respuesta de error del servidor)";
                        }
                        return new DniResponse(false, dni, errorMsg);
                        
                    case 404:
                        return new DniResponse(false, dni, "El DNI no fue encontrado en la base de datos de la API.");
                        
                    case 429:
                        return new DniResponse(false, dni, "Se ha excedido el límite de consultas a la API. Intente más tarde.");
                        
                    default:
                        return new DniResponse(false, dni, "Error HTTP " + ex.getStatusCode() + ": " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error general: " + e.getMessage());
            e.printStackTrace();
            return new DniResponse(false, dni, "Error: " + e.getMessage());
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
