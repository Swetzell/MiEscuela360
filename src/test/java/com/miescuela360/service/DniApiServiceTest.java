package com.miescuela360.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miescuela360.service.DniApiService.DniResponse;

import java.util.Map;

class DniApiServiceTest {

    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private DniApiService dniApiService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(dniApiService, "apiToken", "test-token-123");
        ReflectionTestUtils.setField(dniApiService, "API_URL", "https://api.test.com/dni");
    }
    
    @Test
    void testConsultarDni_Success() throws Exception {
        // Arrange
        String dni = "12345678";
        String jsonResponse = "{\"success\":true,\"dni\":\"12345678\",\"nombre\":\"JUAN PEREZ\"}";
        
        DniResponse expectedResponse = new DniResponse(true, dni, "JUAN PEREZ");
        
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(objectMapper.readValue(eq(jsonResponse), eq(DniResponse.class))).thenReturn(expectedResponse);
        
        // Act
        DniResponse result = dniApiService.consultarDni(dni);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("JUAN PEREZ", result.getNombre());
        assertEquals(dni, result.getDni());
        assertFalse(dniApiService.isTokenFailureDetected(), "Token failure should not be detected");
    }
    
    @Test
    void testConsultarDni_403Forbidden() throws Exception {
        // Arrange
        String dni = "12345678";
        String errorJson = "{\"message\":\"Invalid or expired token\"}";
        
        HttpClientErrorException exception = HttpClientErrorException.create(
            HttpStatus.FORBIDDEN, 
            "Forbidden", 
            null, 
            errorJson.getBytes(), 
            null
        );
        
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(exception);
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(objectMapper.readValue(eq(errorJson), any(com.fasterxml.jackson.core.type.TypeReference.class))).thenReturn(Map.of("message", "Invalid or expired token"));
        
        // Act
        DniResponse result = dniApiService.consultarDni(dni);
        
        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getNombre().contains("token"), "Error message should mention token issues");
        assertTrue(dniApiService.isTokenFailureDetected(), "Token failure should be detected");
    }
    
    @Test
    void testGetApiToken() {
        // Arrange
        String expectedToken = "test-token-123";
        
        // Act
        String token = dniApiService.getApiToken();
        
        // Assert
        assertEquals(expectedToken, token);
    }
}
