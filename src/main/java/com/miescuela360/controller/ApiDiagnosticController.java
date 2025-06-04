package com.miescuela360.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.miescuela360.service.DniApiService;
import com.miescuela360.service.DniApiServiceAlternative;
import com.miescuela360.service.ApiFreeService;
import com.miescuela360.service.DniApiService.DniResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para ejecutar diagnósticos de las APIs de DNI
 * Este controlador verifica todas las implementaciones disponibles
 * y proporciona información detallada para depuración
 */
@RestController
@RequestMapping("/api/diagnose")
public class ApiDiagnosticController {

    @Autowired
    private DniApiService dniApiService;
    
    @Autowired
    private DniApiServiceAlternative dniApiServiceAlt;
    
    @Autowired
    private ApiFreeService apiFreeService;
    
    @Value("${api.dni.priority}")
    private String apiPriorityConfig;
    
    /**
     * Realiza diagnósticos en todas las APIs disponibles
     */
    @GetMapping("/check-all/{dni}")
    public ResponseEntity<Map<String, Object>> checkAllApis(@PathVariable String dni) {
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Verificar API MiGo
            DniApiService.DniResponse migoResponse = dniApiService.consultarDni(dni);
            Map<String, Object> migoResult = new HashMap<>();
            migoResult.put("success", migoResponse.isSuccess());
            migoResult.put("message", migoResponse.getNombre());
            migoResult.put("tokenFailureDetected", dniApiService.isTokenFailureDetected());
            results.put("migoApi", migoResult);
            
            // Verificar API Alternativa
            DniApiServiceAlternative.DniResponse altResponse = dniApiServiceAlt.consultarDni(dni);
            Map<String, Object> altResult = new HashMap<>();
            altResult.put("success", altResponse.isSuccess());
            altResult.put("message", altResponse.getNombre());
            results.put("alternativeApi", altResult);
            
            // Verificar API Free
            DniApiService.DniResponse freeResponse = apiFreeService.consultarDni(dni);
            Map<String, Object> freeResult = new HashMap<>();
            freeResult.put("success", freeResponse.isSuccess());
            freeResult.put("message", freeResponse.getNombre());
            results.put("freeApi", freeResult);
            
            // Resumen: ¿alguna API funcionó?
            boolean anySuccess = migoResponse.isSuccess() || altResponse.isSuccess() || freeResponse.isSuccess();
            results.put("anySuccessful", anySuccess);
            
            // Estrategia recomendada
            String recommendedStrategy = determineRecommendedStrategy(migoResponse, altResponse, freeResponse);
            results.put("recommendedStrategy", recommendedStrategy);
            
            // Añadir información de configuración actual
            results.put("currentPriority", apiPriorityConfig);
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getClass().getName());
            error.put("message", e.getMessage());
            results.put("error", error);
            return ResponseEntity.status(500).body(results);
        }
    }
    
    /**
     * Determina qué API es la más confiable basado en los resultados
     */
    private String determineRecommendedStrategy(
            DniApiService.DniResponse migoResponse, 
            DniApiServiceAlternative.DniResponse altResponse, 
            DniApiService.DniResponse freeResponse) {
        
        int successCount = 0;
        if (migoResponse.isSuccess()) successCount++;
        if (altResponse.isSuccess()) successCount++;
        if (freeResponse.isSuccess()) successCount++;
        
        if (successCount == 0) {
            return "Ninguna API funcionó. Recomendación: Verificar conectividad y tokens.";
        }
        
        // Si el token de MiGo está fallando, priorizamos otras opciones
        if (dniApiService.isTokenFailureDetected()) {
            if (freeResponse.isSuccess()) {
                return "Token MiGo inválido. Usar API gratuita (apis.net.pe) como primera opción";
            } else if (altResponse.isSuccess()) {
                return "Token MiGo inválido. Usar API alternativa como opción viable";
            }
        }
        
        // Estrategia normal cuando todo funciona
        if (freeResponse.isSuccess()) {
            return "Usar API gratuita (apis.net.pe) como primera opción";
        } else if (migoResponse.isSuccess()) {
            return "Usar API MiGo como primera opción";
        } else {
            return "Usar API alternativa como última opción";
        }
    }
    
    /**
     * Verifica solo la API MiGo para diagnosticar problemas específicos
     */
    @GetMapping("/check-migo/{dni}")
    public ResponseEntity<Map<String, Object>> checkMigoApi(@PathVariable String dni) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Verificamos el valor del token (parcial, por seguridad) configurado actualmente
            String tokenValue = "No disponible";
            try {
                String apiToken = dniApiService.getApiToken();
                tokenValue = apiToken.length() > 8 ? 
                    apiToken.substring(0, 4) + "..." + apiToken.substring(apiToken.length() - 4) : apiToken;
            } catch (Exception ex) {
                tokenValue = "Error al obtener token";
            }
            result.put("tokenConfigured", tokenValue);
            
            DniApiService.DniResponse response = dniApiService.consultarDni(dni);
            result.put("success", response.isSuccess());
            result.put("dni", response.getDni());
            result.put("nombre", response.getNombre());
            result.put("timestamp", System.currentTimeMillis());
            result.put("tokenFailureDetected", dniApiService.isTokenFailureDetected());
            
            if (response.isSuccess()) {
                result.put("diagnostico", "La API MiGo funciona correctamente");
            } else {
                if (dniApiService.isTokenFailureDetected() || 
                    (response.getNombre() != null && response.getNombre().toLowerCase().contains("token"))) {
                    result.put("diagnostico", "Problema de token detectado. El token podría estar expirado o ser inválido.");
                    result.put("recomendacion", "Actualice el token en application.properties y reinicie la aplicación");
                } else {
                    result.put("diagnostico", "La API no respondió correctamente pero el error no parece relacionado con el token");
                }
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getClass().getName());
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * Endpoint para probar todas las APIs con un DNI conocido y válido
     * para verificar su correcto funcionamiento
     */
    @GetMapping("/test-with-known-dni")
    public ResponseEntity<Map<String, Object>> testWithKnownDni() {
        // Usamos un DNI conocido y válido para pruebas
        String testDni = "43582167"; // Reemplazar con un DNI válido para pruebas
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Probar todas las APIs
            results.put("migo", checkSpecificApi("migo", testDni));
            results.put("free", checkSpecificApi("free", testDni));
            results.put("alternative", checkSpecificApi("alternative", testDni));
              // Verificar si alguna API está funcionando
            boolean anyWorking = false;
            for (String key : results.keySet()) {
                if (key.equals("migo") || key.equals("free") || key.equals("alternative")) {
                    Object resultObj = results.get(key);
                    if (resultObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> apiResult = (Map<String, Object>) resultObj;
                        if (Boolean.TRUE.equals(apiResult.getOrDefault("success", false))) {
                            anyWorking = true;
                            break;
                        }
                    }
                }
            }
            
            results.put("anyApiWorking", anyWorking);
            if (anyWorking) {
                results.put("diagnostico", "Al menos una API está funcionando correctamente");
            } else {
                results.put("diagnostico", "Ninguna API está funcionando. Revise la conexión a internet o la validez del DNI de prueba");
            }
            
            // Añadir información de configuración actual
            results.put("currentPriority", apiPriorityConfig);
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            results.put("error", e.getMessage());
            return ResponseEntity.status(500).body(results);
        }
    }
    
    /**
     * Método auxiliar para comprobar una API específica
     */
    private Map<String, Object> checkSpecificApi(String apiType, String dni) {
        Map<String, Object> result = new HashMap<>();
        result.put("api", apiType);
        result.put("dni", dni);
        
        try {
            switch (apiType) {
                case "migo":
                    DniApiService.DniResponse migoResponse = dniApiService.consultarDni(dni);
                    result.put("success", migoResponse.isSuccess());
                    result.put("response", migoResponse.getNombre());
                    result.put("tokenFailureDetected", dniApiService.isTokenFailureDetected());
                    break;
                    
                case "free":
                    DniApiService.DniResponse freeResponse = apiFreeService.consultarDni(dni);
                    result.put("success", freeResponse.isSuccess());
                    result.put("response", freeResponse.getNombre());
                    break;
                    
                case "alternative":
                    DniApiServiceAlternative.DniResponse altResponse = dniApiServiceAlt.consultarDni(dni);
                    result.put("success", altResponse.isSuccess());
                    result.put("response", altResponse.getNombre());
                    break;
                    
                default:
                    result.put("success", false);
                    result.put("response", "Tipo de API desconocido");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
