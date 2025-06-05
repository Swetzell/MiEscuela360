                    package com.miescuela360.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para probar directamente las APIs de consulta de DNI
 * Este controlador permitirá diagnosticar problemas con las APIs
 */
@RestController
@RequestMapping("/api-test")
public class ApiTestController {

    @Value("${api.peru.token}")
    private String apiToken;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public ApiTestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Endpoint para probar directamente la API de apiperu.dev
     */
    @GetMapping("/peru/{dni}")
    public ResponseEntity<Object> testPeruApi(@PathVariable String dni) {
        try {
            String apiUrl = "https://apiperu.dev/api/dni";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + apiToken);
            headers.set("User-Agent", "MiEscuela360-JavaClient/1.0");
            
            // Log del token enmascarado (por seguridad)
            String tokenMasked = apiToken.length() > 8 ? 
                apiToken.substring(0, 4) + "..." + apiToken.substring(apiToken.length() - 4) : apiToken;
            System.out.println("Probando API Peru con token parcial: " + tokenMasked);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("dni", dni);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            // Registramos información detallada para diagnóstico
            System.out.println("URL: " + apiUrl);
            System.out.println("Método: POST");
            System.out.println("Headers: " + headers);
            
            // Ejecutamos la llamada directamente y devolvemos la respuesta raw
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.POST, 
                request, 
                String.class
            );
            
            System.out.println("Respuesta exitosa. Código: " + response.getStatusCode());
            
            // Procesar la respuesta
            JsonNode responseBody = objectMapper.readTree(response.getBody());
            
            // Incluir información de diagnóstico en la respuesta
            Map<String, Object> enhancedResponse = new HashMap<>();
            enhancedResponse.put("apiResponse", responseBody);
            enhancedResponse.put("status", response.getStatusCode().value());
            enhancedResponse.put("success", responseBody.path("success").asBoolean());
            
            if (responseBody.path("success").asBoolean()) {
                enhancedResponse.put("diagnosticInfo", "La API respondió correctamente");
                enhancedResponse.put("data", responseBody.path("data"));
            } else {
                enhancedResponse.put("diagnosticInfo", responseBody.path("message").asText("Error desconocido"));
            }
            
            return ResponseEntity.ok()
                .header("X-Api-Status", "SUCCESS")
                .header("X-Response-Code", response.getStatusCode().toString())
                .body(enhancedResponse);
            
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getClass().getName());
            errorResponse.put("status", ex.getStatusCode().value());
            
            // Intentar extraer el mensaje de error del cuerpo de la respuesta
            String errorMessage = ex.getResponseBodyAsString();
            try {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    JsonNode errorNode = objectMapper.readTree(errorMessage);
                    errorMessage = errorNode.has("message") ? 
                        errorNode.get("message").asText() : 
                        ex.getStatusText();
                } else {
                    errorMessage = ex.getStatusText();
                }
            } catch (Exception e) {
                errorMessage = ex.getStatusText();
            }
            
            errorResponse.put("message", errorMessage);
            
            // Manejo específico para errores HTTP
            String diagnosticMsg = "";
            switch (ex.getStatusCode().value()) {
                case 400:
                    diagnosticMsg = "Error en la solicitud: es posible que el formato del DNI sea incorrecto.";
                    break;
                case 401:
                    diagnosticMsg = "Error de autenticación: el token no fue reconocido o ha expirado.";
                    errorResponse.put("recomendacion", "Verifique que el token sea correcto y esté activo.");
                    break;
                case 403:
                    diagnosticMsg = "Acceso prohibido: verifique los permisos del token.";
                    errorResponse.put("recomendacion", "Verifique que el token tenga los permisos necesarios.");
                    break;
                case 404:
                    diagnosticMsg = "DNI no encontrado en la base de datos de la API.";
                    break;
                case 429:
                    diagnosticMsg = "Se ha excedido el límite de consultas a la API.";
                    errorResponse.put("recomendacion", "Espere unos minutos antes de realizar más consultas o actualice su plan.");
                    break;
                default:
                    diagnosticMsg = "Error en la comunicación con la API.";
            }
            
            errorResponse.put("diagnosticInfo", diagnosticMsg);
            
            return ResponseEntity.status(ex.getStatusCode())
                .header("X-Api-Status", "ERROR")
                .header("X-Error-Code", String.valueOf(ex.getStatusCode().value()))
                .body(errorResponse);
                
        } catch (Exception ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getClass().getName());
            errorResponse.put("message", ex.getMessage());
            errorResponse.put("diagnosticInfo", "Error inesperado al procesar la solicitud");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Api-Status", "ERROR")
                .body(errorResponse);
        }
    }
}
