package com.blibli.training.framework.configurations.advisor;

import com.blibli.training.framework.dto.BaseResponse;
import com.blibli.training.framework.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ErrorAdvisorTest {

    private ErrorAdvisor errorAdvisor;

    @BeforeEach
    void setUp() {
        errorAdvisor = new ErrorAdvisor();
    }

    // Tests for handleAuthenticationException

    @Test
    void handleAuthenticationException_ShouldReturnUnauthorizedResponse() {
        // Given
        String errorMessage = "Invalid username or password";
        AuthenticationException exception = new AuthenticationException(errorMessage);

        // When
        BaseResponse<?> response = errorAdvisor.handleAuthenticationException(exception);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getCode());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void handleAuthenticationException_WithDifferentMessage_ShouldReturnCorrectMessage() {
        // Given
        String errorMessage = "Token expired";
        AuthenticationException exception = new AuthenticationException(errorMessage);

        // When
        BaseResponse<?> response = errorAdvisor.handleAuthenticationException(exception);

        // Then
        assertEquals(errorMessage, response.getMessage());
        assertEquals(401, response.getCode());
    }

    @Test
    void handleAuthenticationException_ShouldReturn401StatusCode() {
        // Given
        AuthenticationException exception = new AuthenticationException("Access denied");

        // When
        BaseResponse<?> response = errorAdvisor.handleAuthenticationException(exception);

        // Then
        assertEquals(401, response.getCode());
        assertFalse(response.isSuccess());
    }

    // Tests for handleRuntimeException

    @Test
    void handleRuntimeException_ShouldReturnBadRequestResponse() {
        // Given
        String errorMessage = "Invalid input data";
        RuntimeException exception = new RuntimeException(errorMessage);

        // When
        BaseResponse<?> response = errorAdvisor.handleRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getCode());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void handleRuntimeException_WithDifferentMessage_ShouldReturnCorrectMessage() {
        // Given
        String errorMessage = "Resource not found";
        RuntimeException exception = new RuntimeException(errorMessage);

        // When
        BaseResponse<?> response = errorAdvisor.handleRuntimeException(exception);

        // Then
        assertEquals(errorMessage, response.getMessage());
        assertEquals(400, response.getCode());
    }

    @Test
    void handleRuntimeException_ShouldReturn400StatusCode() {
        // Given
        RuntimeException exception = new RuntimeException("Validation failed");

        // When
        BaseResponse<?> response = errorAdvisor.handleRuntimeException(exception);

        // Then
        assertEquals(400, response.getCode());
        assertFalse(response.isSuccess());
    }

    @Test
    void handleRuntimeException_WithNullPointerException_ShouldHandleCorrectly() {
        // Given
        NullPointerException exception = new NullPointerException("Null value encountered");

        // When
        BaseResponse<?> response = errorAdvisor.handleRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Null value encountered", response.getMessage());
    }

    @Test
    void handleRuntimeException_WithIllegalArgumentException_ShouldHandleCorrectly() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        BaseResponse<?> response = errorAdvisor.handleRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Invalid argument", response.getMessage());
    }

    // Tests for handleException (generic Exception)

    @Test
    void handleException_ShouldReturnInternalServerErrorResponse() {
        // Given
        String errorMessage = "An unexpected error occurred";
        Exception exception = new Exception(errorMessage);

        // When
        BaseResponse<?> response = errorAdvisor.handleException(exception);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getCode());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void handleException_WithDifferentMessage_ShouldReturnCorrectMessage() {
        // Given
        String errorMessage = "Database connection failed";
        Exception exception = new Exception(errorMessage);

        // When
        BaseResponse<?> response = errorAdvisor.handleException(exception);

        // Then
        assertEquals(errorMessage, response.getMessage());
        assertEquals(500, response.getCode());
    }

    @Test
    void handleException_ShouldReturn500StatusCode() {
        // Given
        Exception exception = new Exception("Server error");

        // When
        BaseResponse<?> response = errorAdvisor.handleException(exception);

        // Then
        assertEquals(500, response.getCode());
        assertFalse(response.isSuccess());
    }

    // Edge case tests

    @Test
    void handleAuthenticationException_WithNullMessage_ShouldHandleGracefully() {
        // Given
        AuthenticationException exception = new AuthenticationException(null);

        // When
        BaseResponse<?> response = errorAdvisor.handleAuthenticationException(exception);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(401, response.getCode());
        assertNull(response.getMessage());
    }

    @Test
    void handleRuntimeException_WithEmptyMessage_ShouldHandleGracefully() {
        // Given
        RuntimeException exception = new RuntimeException("");

        // When
        BaseResponse<?> response = errorAdvisor.handleRuntimeException(exception);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("", response.getMessage());
    }

    @Test
    void handleException_WithEmptyMessage_ShouldHandleGracefully() {
        // Given
        Exception exception = new Exception("");

        // When
        BaseResponse<?> response = errorAdvisor.handleException(exception);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(500, response.getCode());
        assertEquals("", response.getMessage());
    }

    @Test
    void allHandlers_ShouldReturnSuccessFalse() {
        // Given
        AuthenticationException authEx = new AuthenticationException("Auth error");
        RuntimeException runtimeEx = new RuntimeException("Runtime error");
        Exception generalEx = new Exception("General error");

        // When
        BaseResponse<?> authResponse = errorAdvisor.handleAuthenticationException(authEx);
        BaseResponse<?> runtimeResponse = errorAdvisor.handleRuntimeException(runtimeEx);
        BaseResponse<?> generalResponse = errorAdvisor.handleException(generalEx);

        // Then
        assertFalse(authResponse.isSuccess());
        assertFalse(runtimeResponse.isSuccess());
        assertFalse(generalResponse.isSuccess());
    }

    @Test
    void allHandlers_ShouldReturnNullData() {
        // Given
        AuthenticationException authEx = new AuthenticationException("Auth error");
        RuntimeException runtimeEx = new RuntimeException("Runtime error");
        Exception generalEx = new Exception("General error");

        // When
        BaseResponse<?> authResponse = errorAdvisor.handleAuthenticationException(authEx);
        BaseResponse<?> runtimeResponse = errorAdvisor.handleRuntimeException(runtimeEx);
        BaseResponse<?> generalResponse = errorAdvisor.handleException(generalEx);

        // Then
        assertNull(authResponse.getData());
        assertNull(runtimeResponse.getData());
        assertNull(generalResponse.getData());
    }

    @Test
    void handleException_WithSpecialCharactersInMessage_ShouldPreserveMessage() {
        // Given
        String specialMessage = "Error: <script>alert('xss')</script> & \"quotes\" 'apostrophe'";
        Exception exception = new Exception(specialMessage);

        // When
        BaseResponse<?> response = errorAdvisor.handleException(exception);

        // Then
        assertEquals(specialMessage, response.getMessage());
    }

    @Test
    void handleRuntimeException_WithLongMessage_ShouldPreserveFullMessage() {
        // Given
        String longMessage = "A".repeat(1000);
        RuntimeException exception = new RuntimeException(longMessage);

        // When
        BaseResponse<?> response = errorAdvisor.handleRuntimeException(exception);

        // Then
        assertEquals(longMessage, response.getMessage());
        assertEquals(1000, response.getMessage().length());
    }
}

