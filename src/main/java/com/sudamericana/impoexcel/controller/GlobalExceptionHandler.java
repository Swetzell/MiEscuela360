package com.sudamericana.impoexcel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, Model model) {
        logger.error("Método HTTP no soportado: {}", ex.getMessage());
        model.addAttribute("error", "Método HTTP no soportado: " + ex.getMessage());
        return "error";
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        logger.error("Error no controlado: {}", ex.getMessage(), ex);
        model.addAttribute("error", "Se produjo un error: " + ex.getMessage());
        return "error";
    }
}
