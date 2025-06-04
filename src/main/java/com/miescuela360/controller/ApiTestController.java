package com.miescuela360.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para probar directamente las APIs de consulta de DNI
 * Este controlador permitirá diagnosticar problemas con las APIs
 */
@RestController
@RequestMapping("/api-test")
public class ApiTestController {

    @Value("${api.migo.token}")
    private String apiToken;
    
    private final RestTemplate restTemplate;
    
    public ApiTestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
      /**
     * Endpoint para probar directamente la API de MiGo
     */
    @GetMapping("/migo/{dni}")
    public ResponseEntity<Object> testMigoApi(@PathVariable String dni) {
        try {
            String apiUrl = "https://api.migo.pe/api/v1/dni";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "MiEscuela360-JavaClient/1.0");
            
            // Log del token enmascarado (por seguridad)
            String tokenMasked = apiToken.length() > 8 ? 
                apiToken.substring(0, 4) + "..." + apiToken.substring(apiToken.length() - 4) : apiToken;
            System.out.println("Probando API MiGo con token parcial: " + tokenMasked);
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("token", apiToken);
            requestBody.put("dni", dni);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            
            // Registramos información detallada para diagnóstico
            System.out.println("URL: " + apiUrl);
            System.out.println("Método: POST");
            System.out.println("Headers: " + headers);
            
            // Ejecutamos la llamada directamente y devolvemos la respuesta raw
            ResponseEntity<Object> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.POST, 
                request, 
                Object.class
            );
            
            System.out.println("Respuesta exitosa. Código: " + response.getStatusCode());
            
            // Incluir información de diagnóstico en la respuesta
            Map<String, Object> enhancedResponse = new HashMap<>();
            enhancedResponse.put("apiResponse", response.getBody());
            enhancedResponse.put("status", response.getStatusCode().value());
            enhancedResponse.put("success", true);
            enhancedResponse.put("diagnosticInfo", "La API respondió correctamente");
            
            return ResponseEntity.ok()
                .header("X-Api-Status", "SUCCESS")
                .header("X-Response-Code", response.getStatusCode().toString())
                .body(enhancedResponse);
            
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getClass().getName());
            errorResponse.put("status", ex.getStatusCode().value());
            errorResponse.put("message", ex.getMessage());
            
            // Manejo específico para errores HTTP
            String diagnosticMsg = "";
            switch (ex.getStatusCode().value()) {
                case 400:
                    diagnosticMsg = "Error en la solicitud: es posible que el formato del DNI sea incorrecto.";
                    break;
                case 401:
                    diagnosticMsg = "Error de autenticación: el token no fue reconocido.";
                    break;
                case 403:
                    diagnosticMsg = "Acceso prohibido: el token probablemente ha expirado o ha sido revocado.";
                    errorResponse.put("recomendacion", "Contacte al proveedor de la API MiGo para renovar su token.");
                    break;
                case 404:
                    diagnosticMsg = "DNI no encontrado en la base de datos de la API.";
                    break;
                case 429:
                    diagnosticMsg = "Se ha excedido el límite de consultas a la API.";
                    break;
                default:
                    diagnosticMsg = "Error HTTP " + ex.getStatusCode() + ": " + ex.getMessage();
            }
            
            errorResponse.put("diagnosticInfo", diagnosticMsg);
            
            // Tratar de extraer más información del cuerpo de la respuesta
            try {
                if (ex.getResponseBodyAsString() != null && !ex.getResponseBodyAsString().isEmpty()) {
                    errorResponse.put("apiErrorResponse", ex.getResponseBodyAsString());
                }
            } catch (Exception e) {
                errorResponse.put("parsingError", "No se pudo extraer el mensaje de error detallado");
            }
            
            return ResponseEntity
                .status(ex.getStatusCode())
                .header("X-Api-Status", "ERROR")
                .header("X-Error-Code", String.valueOf(ex.getStatusCode().value()))
                .body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getClass().getName());
            errorResponse.put("message", e.getMessage());
            errorResponse.put("diagnosticInfo", "Error no relacionado con HTTP: " + e.getMessage());
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Api-Status", "ERROR")
                .header("X-Error-Type", e.getClass().getName())
                .body(errorResponse);
        }
    }
    
    /**
     * Endpoint para probar API gratuita alternativa
     */
    @GetMapping("/free/{dni}")
    public ResponseEntity<Object> testFreeApi(@PathVariable String dni) {
        try {
            String apiUrl = "https://api.apis.net.pe/v1/dni/" + dni;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            // Ejecutamos la llamada directamente y devolvemos la respuesta raw
            ResponseEntity<Object> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.GET, 
                request, 
                Object.class
            );
            
            return ResponseEntity.ok()
                .header("X-Api-Status", "SUCCESS")
                .header("X-Response-Code", response.getStatusCode().toString())
                .body(response.getBody());
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getClass().getName());
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Api-Status", "ERROR")
                .header("X-Error-Type", e.getClass().getName())
                .body(errorResponse);
        }
    }
}
