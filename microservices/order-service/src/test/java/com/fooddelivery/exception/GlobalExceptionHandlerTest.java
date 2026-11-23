package com.fooddelivery.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    @Test
    void serviceUnavailableMapsTo503WithMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response = handler.handleServiceUnavailable(
                new ServiceUnavailableException("Customer service is unavailable")
        );

        assertEquals(503, response.getStatusCode().value());
        assertTrue(response.getBody() != null);
        assertEquals("Customer service is unavailable", response.getBody().get("message"));
    }
}
