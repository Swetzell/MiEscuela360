package com.sudamericana.impoexcel.config;

import com.sudamericana.impoexcel.model.Almacen;
import com.sudamericana.impoexcel.service.AlmacenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {
    
    private final AlmacenService almacenService;
    
    @Autowired
    public GlobalModelAttributes(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }
    
    @ModelAttribute("almacenes")
    public List<Almacen> getAlmacenes() {
        return almacenService.listarAlmacenes();
    }
}