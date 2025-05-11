package com.sudamericana.impoexcel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.time.Duration;

@Configuration
public class AppConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000); // 15 segundos
        factory.setReadTimeout(30000);    // 30 segundos
        
        return new RestTemplateBuilder()
            .requestFactory(() -> factory)
            .errorHandler(new CustomErrorHandler())
            .setConnectTimeout(Duration.ofMillis(15000))
            .setReadTimeout(Duration.ofMillis(30000))
            .build();
    }
    
    private static class CustomErrorHandler implements ResponseErrorHandler {
        
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().isError();
        }
        
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            logger.error("Error en la llamada a la API externa: Código de estado HTTP {}", 
                    response.getStatusCode().value());
            
            byte[] body = response.getBody().readAllBytes();
            if (body.length > 0) {
                logger.error("Cuerpo de la respuesta de error: {}", new String(body));
            }
        }
    }
}