package com.miescuela360.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.miescuela360.service.ApiFreeService;
import com.miescuela360.service.DniApiService;
import com.miescuela360.service.DniApiServiceAlternative;
import com.miescuela360.service.DniApiService.DniResponse;

import java.util.Map;

class ApiDiagnosticControllerTest {

    @Mock
    private DniApiService dniApiService;
    
    @Mock
    private DniApiServiceAlternative dniApiServiceAlt;
    
    @Mock
    private ApiFreeService apiFreeService;
    
    @InjectMocks
    private ApiDiagnosticController controller;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testCheckMigoApi_Success() {
        // Arrange
        String dni = "12345678";
        String nombre = "JUAN PEREZ";
        
        when(dniApiService.getApiToken()).thenReturn("test-token-123");
        when(dniApiService.isTokenFailureDetected()).thenReturn(false);
        when(dniApiService.consultarDni(dni)).thenReturn(new DniResponse(true, dni, nombre));
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.checkMigoApi(dni);
        
        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals(dni, body.get("dni"));
        assertEquals(nombre, body.get("nombre"));
        assertEquals("La API MiGo funciona correctamente", body.get("diagnostico"));
    }
    
    @Test
    void testCheckMigoApi_TokenFailure() {
        // Arrange
        String dni = "12345678";
        String errorMessage = "Error de acceso (403): El token API MiGo parece ser inválido";
        
        when(dniApiService.getApiToken()).thenReturn("test-token-123");
        when(dniApiService.isTokenFailureDetected()).thenReturn(true);
        when(dniApiService.consultarDni(dni)).thenReturn(new DniResponse(false, dni, errorMessage));
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.checkMigoApi(dni);
        
        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals(dni, body.get("dni"));
        assertTrue(((String) body.get("diagnostico")).contains("token"));
        assertTrue((Boolean) body.get("tokenFailureDetected"));
    }
    
    @Test
    void testCheckAllApis() {
        // Arrange
        String dni = "12345678";
        
        when(dniApiService.getApiToken()).thenReturn("test-token-123");
        when(dniApiService.consultarDni(dni)).thenReturn(new DniResponse(false, dni, "Token error"));
        when(dniApiService.isTokenFailureDetected()).thenReturn(true);
        
        when(apiFreeService.consultarDni(dni)).thenReturn(new DniResponse(true, dni, "JUAN PEREZ"));
        
        DniApiServiceAlternative.DniResponse altResponse = new DniApiServiceAlternative.DniResponse(true, dni, "JUAN PEREZ ALT");
        when(dniApiServiceAlt.consultarDni(dni)).thenReturn(altResponse);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.checkAllApis(dni);
        
        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> migoResult = (Map<String, Object>) body.get("migoApi");
        assertFalse((Boolean) migoResult.get("success"));
        assertTrue((Boolean) migoResult.get("tokenFailureDetected"));
        
        assertTrue((Boolean) body.get("anySuccessful"));
    }
}
