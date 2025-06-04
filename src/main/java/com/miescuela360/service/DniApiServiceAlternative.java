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
public class DniApiServiceAlternative {    // API gratuita para consulta de DNI (API pública sin autenticación)
    private final String API_URL = "https://api.apis.net.pe/v1/dni";private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;    // Inyectamos el bean específico para APIs
    public DniApiServiceAlternative(@org.springframework.beans.factory.annotation.Qualifier("apiRestTemplate") RestTemplate restTemplate, 
                                   @org.springframework.beans.factory.annotation.Qualifier("objectMapperWithJavaTime") ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }    public DniResponse consultarDni(String dni) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Esta API acepta JSON o puede funcionar sin especificar el Content-Type
            headers.set("Accept", "application/json");
            
            System.out.println("Consultando DNI con API alternativa gratuita: " + dni);
            
            // API gratuita que solo requiere el DNI como parámetro en la URL
            String url = API_URL + "/" + dni;
            
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                
                System.out.println("API alternativa - Código de respuesta: " + response.getStatusCode());
                System.out.println("API alternativa - Cuerpo: " + response.getBody());
                  if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // Parse manually since the response format is different
                    Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), 
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                    if (responseMap != null && responseMap.containsKey("name")) {
                        String nombre = (String) responseMap.get("name");
                        return new DniResponse(true, dni, nombre);
                    } else {
                        return new DniResponse(false, dni, null);
                    }
                } else {
                    return new DniResponse(false, dni, null);
                }
            } catch (Exception ex) {
                System.out.println("Error en API alternativa: " + ex.getMessage());
                return new DniResponse(false, dni, "Error API alternativa: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error general API alternativa: " + e.getMessage());
            e.printStackTrace();
            return new DniResponse(false, dni, "Error general: " + e.getMessage());
        }
    }

    public static class DniResponse {
        private boolean success;
        private String dni;
        private String nombre;

        public DniResponse() {
        }

        public DniResponse(boolean success, String dni, String nombre) {
            this.success = success;
            this.dni = dni;
            this.nombre = nombre;
        }

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
