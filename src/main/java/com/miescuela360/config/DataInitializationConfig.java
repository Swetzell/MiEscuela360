package com.miescuela360.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.miescuela360.service.GradoService;
import com.miescuela360.service.SeccionService;

@Configuration
public class DataInitializationConfig {
    
    @Autowired
    private GradoService gradoService;
    
    @Autowired
    private SeccionService seccionService;
    
    @Bean
    CommandLineRunner initData() {
        return args -> {
            // Inicializar datos por defecto
            gradoService.initDefaultData();
            seccionService.initDefaultData();
            
            System.out.println("Datos iniciales de grados y secciones cargados correctamente");
        };
    }
}
