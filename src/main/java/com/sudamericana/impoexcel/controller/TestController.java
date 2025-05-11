package com.sudamericana.impoexcel.controller;

import com.sudamericana.impoexcel.model.Vendedor;
import com.sudamericana.impoexcel.service.VendedorServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private final VendedorServiceImpl vendedorService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TestController(VendedorServiceImpl vendedorService, JdbcTemplate jdbcTemplate) {
        this.vendedorService = vendedorService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/vendedores")
    public List<Vendedor> testVendedores() {
        logger.info("Testing vendedores service");
        List<Vendedor> vendedores = vendedorService.listarTodos();
        logger.info("Retrieved {} vendedores", vendedores.size());
        return vendedores;
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            status.put("database", result != null && result == 1 ? "OK" : "ERROR");
            
            List<Vendedor> vendedores = vendedorService.listarTodos();
            status.put("vendedores", vendedores != null ? "OK" : "ERROR");
            status.put("vendedores_count", vendedores != null ? vendedores.size() : 0);
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(status);
        }
    }
}
