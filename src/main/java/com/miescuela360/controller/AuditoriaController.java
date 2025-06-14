package com.miescuela360.controller;

import com.miescuela360.model.Auditoria;
import com.miescuela360.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @GetMapping
    public String listarAuditoria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String accion,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, 20, Sort.by("fechaHora").descending());
        Page<Auditoria> paginaAuditoria;
        
        if (entidad != null && !entidad.isEmpty() && accion != null && !accion.isEmpty()) {
            paginaAuditoria = auditoriaRepository.findByEntidadAndAccion(entidad, accion, pageable);
        } else if (entidad != null && !entidad.isEmpty()) {
            paginaAuditoria = auditoriaRepository.findByEntidad(entidad, pageable);
        } else if (accion != null && !accion.isEmpty()) {
            paginaAuditoria = auditoriaRepository.findByAccion(accion, pageable);
        } else {
            paginaAuditoria = auditoriaRepository.findAll(pageable);
        }
        
        model.addAttribute("paginaAuditoria", paginaAuditoria);
        model.addAttribute("entidades", auditoriaRepository.findDistinctEntidades());
        model.addAttribute("acciones", auditoriaRepository.findDistinctAcciones());
        model.addAttribute("entidadSeleccionada", entidad != null ? entidad : "");
        model.addAttribute("accionSeleccionada", accion != null ? accion : "");
        
        return "auditoria/index";
    }

}
