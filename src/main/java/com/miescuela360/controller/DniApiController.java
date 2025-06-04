package com.miescuela360.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.miescuela360.service.DniApiService;
import com.miescuela360.service.DniApiService.DniResponse;

@RestController
@RequestMapping("/api/dni")
public class DniApiController {

    @Autowired
    private DniApiService dniApiService;

    @PostMapping("/consultar")
    public ResponseEntity<DniResponse> consultarDni(@RequestBody DniConsultaRequest request) {
        DniResponse response = dniApiService.consultarDni(request.getDni());
        return ResponseEntity.ok(response);
    }

    public static class DniConsultaRequest {
        private String dni;

        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }
    }
}
