package com.miescuela360.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Servicio alternativo para consultar DNIs usando una API gratuita.
 * Esta implementación utiliza la API pública de apis.net.pe
 */
@Service
public class ApiFreeService {

    // API gratuita para consulta de DNI
    private final String API_URL = "https://api.apis.net.pe/v1/dni";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Inyectamos los beans específicos
    public ApiFreeService(@org.springframework.beans.factory.annotation.Qualifier("apiRestTemplate") RestTemplate restTemplate,
                          @org.springframework.beans.factory.annotation.Qualifier("objectMapperWithJavaTime") ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Consulta un DNI en la API gratuita
     */
    public DniApiService.DniResponse consultarDni(String dni) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            
            System.out.println("Consultando DNI con API gratuita: " + dni);
            String url = API_URL + "/" + dni;
            
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                System.out.println("API gratuita - Código: " + response.getStatusCode());
                System.out.println("API gratuita - Cuerpo: " + response.getBody());
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseMap = objectMapper.readValue(response.getBody(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                    
                    if (responseMap != null) {
                        // Construir el nombre completo con el formato: APELLIDOS NOMBRES
                        StringBuilder nombreCompleto = new StringBuilder();
                        
                        // Primero los apellidos
                        if (responseMap.containsKey("apellidoPaterno")) {
                            nombreCompleto.append(responseMap.get("apellidoPaterno"));
                        }
                        if (responseMap.containsKey("apellidoMaterno")) {
                            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
                            nombreCompleto.append(responseMap.get("apellidoMaterno"));
                        }
                        
                        // Luego el nombre
                        if (responseMap.containsKey("nombres")) {
                            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
                            nombreCompleto.append(responseMap.get("nombres"));
                        }
                        
                        if (nombreCompleto.length() > 0) {
                            return new DniApiService.DniResponse(true, dni, nombreCompleto.toString().toUpperCase());
                        }
                    }
                    
                    return new DniApiService.DniResponse(false, dni, "No se encontró información para el DNI proporcionado");
                } else {
                    return new DniApiService.DniResponse(false, dni, "La API no devolvió una respuesta válida");
                }
            } catch (Exception ex) {
                System.out.println("Error en API gratuita: " + ex.getMessage());
                ex.printStackTrace();
                return new DniApiService.DniResponse(false, dni, "Error: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error general en API gratuita: " + e.getMessage());
            e.printStackTrace();
            return new DniApiService.DniResponse(false, dni, "Error general: " + e.getMessage());
        }
    }
}
