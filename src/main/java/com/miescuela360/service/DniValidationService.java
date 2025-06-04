package com.miescuela360.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar la validación de DNI utilizando múltiples APIs
 * Este servicio implementa una estrategia de fallback entre varias APIs
 * según la configuración de prioridad en application.properties
 */
@Service
public class DniValidationService {

    @Value("${api.dni.priority:apis-net-pe,migo,alternative}")
    private String apiPriorityConfig;
    
    @Autowired
    private DniApiService dniApiService;
    
    @Autowired
    private DniApiServiceAlternative dniApiServiceAlt;
    
    @Autowired
    private ApiFreeService apiFreeService;
    
    /**
     * Consulta un DNI utilizando las APIs configuradas en orden de prioridad
     * @param dni DNI a consultar
     * @return La primera respuesta exitosa de cualquier API, o un error si ninguna API funcionó
     */
    public DniApiService.DniResponse validateDni(String dni) {
        System.out.println("Validando DNI con prioridad: " + apiPriorityConfig);
        
        // Verificar si el token MiGo está fallando para ajustar la estrategia
        List<String> apiPriority = parseApiPriority();
        
        // Si el token MiGo está marcado como fallido, reorganizar la prioridad
        // para evitar intentar usar MiGo primero
        if (dniApiService.isTokenFailureDetected()) {
            System.out.println("Token MiGo marcado como fallido, ajustando estrategia de API");
            apiPriority = apiPriority.stream()
                .filter(api -> !api.equals("migo"))
                .collect(Collectors.toList());
            // Agregar MiGo al final como último recurso
            apiPriority.add("migo");
            System.out.println("Nueva prioridad tras detección de fallo de token: " + apiPriority);
        }
        
        DniApiService.DniResponse lastResponse = null;
        
        for (String api : apiPriority) {
            try {
                DniApiService.DniResponse response = null;
                
                switch (api) {
                    case "migo":
                        System.out.println("Intentando API MiGo...");
                        response = dniApiService.consultarDni(dni);
                        break;
                        
                    case "apis-net-pe":
                        System.out.println("Intentando API gratuita (apis.net.pe)...");
                        response = apiFreeService.consultarDni(dni);
                        break;
                        
                    case "alternative":
                        System.out.println("Intentando API alternativa...");
                        DniApiServiceAlternative.DniResponse altResponse = dniApiServiceAlt.consultarDni(dni);
                        if (altResponse != null) {
                            response = new DniApiService.DniResponse(
                                altResponse.isSuccess(),
                                altResponse.getDni(),
                                altResponse.getNombre()
                            );
                        }
                        break;
                }
                
                // Si la respuesta es exitosa, la devolvemos inmediatamente
                if (response != null && response.isSuccess()) {
                    System.out.println("API " + api + " respondió exitosamente: " + response.getNombre());
                    return response;
                }
                
                // Guardamos la última respuesta para devolverla si todas las APIs fallan
                if (response != null) {
                    lastResponse = response;
                }
                
            } catch (Exception e) {
                System.out.println("Error al consultar API " + api + ": " + e.getMessage());
                // Continuamos con la siguiente API
            }
        }
        
        // Si no hubo respuestas exitosas, devolvemos la última respuesta o una genérica
        if (lastResponse != null) {
            return lastResponse;
        } else {
            return new DniApiService.DniResponse(
                false,
                dni,
                "No se pudo validar el DNI en ninguna de las APIs disponibles"
            );
        }
    }
    
    /**
     * Parsea la configuración de prioridad de APIs
     */
    private List<String> parseApiPriority() {
        if (apiPriorityConfig == null || apiPriorityConfig.trim().isEmpty()) {
            // Default priority if none is configured
            return Arrays.asList("apis-net-pe", "migo", "alternative");
        }
        
        return Arrays.asList(apiPriorityConfig.split(","))
            .stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}
