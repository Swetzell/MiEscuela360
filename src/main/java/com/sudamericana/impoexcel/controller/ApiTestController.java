package com.sudamericana.impoexcel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api-test")
public class ApiTestController {

    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${api.key:GZvVtHI3GzO0DdnwkpLboa3m2U8ogf0TnTtpK8nUtpUDq2jjjji5G6utZMdZ}")
    private String apiKey;
    
    @GetMapping("/ruc/{ruc}")
    public String testRucApi(@PathVariable String ruc) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.migo.pe/api/v1/ruc/" + ruc, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            return "Código de estado: " + response.getStatusCode() + 
                   "\nCuerpo: " + response.getBody();
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\nCausa: " + 
                   (e.getCause() != null ? e.getCause().getMessage() : "desconocida");
        }
    }
}