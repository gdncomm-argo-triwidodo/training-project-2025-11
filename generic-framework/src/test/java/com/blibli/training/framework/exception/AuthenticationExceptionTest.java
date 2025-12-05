package com.blibli.training.framework.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationExceptionTest {

    @Test
    void constructor_WithMessage_ShouldSetMessage() {
        // Given
        String errorMessage = "Invalid credentials";

        // When
        AuthenticationException exception = new AuthenticationException(errorMessage);

        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void exception_ShouldBeRuntimeException() {
        // Given
        AuthenticationException exception = new AuthenticationException("Test");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exception_WithDifferentMessages_ShouldReturnCorrectMessage() {
        // Given
        String message1 = "Invalid username or password";
        String message2 = "Token expired";
        String message3 = "Unauthorized access";

        // When
        AuthenticationException exception1 = new AuthenticationException(message1);
        AuthenticationException exception2 = new AuthenticationException(message2);
        AuthenticationException exception3 = new AuthenticationException(message3);

        // Then
        assertEquals(message1, exception1.getMessage());
        assertEquals(message2, exception2.getMessage());
        assertEquals(message3, exception3.getMessage());
    }

    @Test
    void exception_WithNullMessage_ShouldAcceptNull() {
        // When
        AuthenticationException exception = new AuthenticationException(null);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void exception_WithEmptyMessage_ShouldAcceptEmptyString() {
        // Given
        String emptyMessage = "";

        // When
        AuthenticationException exception = new AuthenticationException(emptyMessage);

        // Then
        assertNotNull(exception);
        assertEquals(emptyMessage, exception.getMessage());
    }
}

