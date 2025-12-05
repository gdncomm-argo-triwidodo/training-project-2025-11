package com.blibli.training.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthPropertiesTest {

    private AuthProperties authProperties;

    @BeforeEach
    void setUp() {
        authProperties = new AuthProperties();
    }

    @Test
    void getPublicPaths_WhenInitialized_ShouldReturnEmptyList() {
        // When
        List<String> publicPaths = authProperties.getPublicPaths();

        // Then
        assertNotNull(publicPaths);
        assertTrue(publicPaths.isEmpty());
    }

    @Test
    void setPublicPaths_ShouldSetPaths() {
        // Given
        List<String> paths = Arrays.asList("/api/public", "/health", "/actuator");

        // When
        authProperties.setPublicPaths(paths);

        // Then
        assertEquals(paths, authProperties.getPublicPaths());
        assertEquals(3, authProperties.getPublicPaths().size());
    }

    @Test
    void publicPaths_ShouldBeModifiable() {
        // Given
        List<String> paths = new java.util.ArrayList<>(Arrays.asList("/api/public"));
        authProperties.setPublicPaths(paths);

        // When
        authProperties.getPublicPaths().add("/health");

        // Then
        assertEquals(2, authProperties.getPublicPaths().size());
        assertTrue(authProperties.getPublicPaths().contains("/api/public"));
        assertTrue(authProperties.getPublicPaths().contains("/health"));
    }

    @Test
    void publicPaths_CanContainMultiplePaths() {
        // Given
        List<String> paths = Arrays.asList(
            "/api/member/register",
            "/api/member/login",
            "/api/products/public",
            "/health",
            "/actuator/**"
        );

        // When
        authProperties.setPublicPaths(paths);

        // Then
        assertEquals(5, authProperties.getPublicPaths().size());
        assertTrue(authProperties.getPublicPaths().containsAll(paths));
    }

    @Test
    void publicPaths_CanBeCleared() {
        // Given
        List<String> paths = new java.util.ArrayList<>(Arrays.asList("/api/public", "/health"));
        authProperties.setPublicPaths(paths);

        // When
        authProperties.getPublicPaths().clear();

        // Then
        assertTrue(authProperties.getPublicPaths().isEmpty());
    }

    @Test
    void publicPaths_ShouldHandleEmptyList() {
        // Given
        List<String> emptyList = Arrays.asList();

        // When
        authProperties.setPublicPaths(emptyList);

        // Then
        assertNotNull(authProperties.getPublicPaths());
        assertTrue(authProperties.getPublicPaths().isEmpty());
    }

    @Test
    void publicPaths_ShouldHandleSinglePath() {
        // Given
        List<String> singlePath = Arrays.asList("/api/public");

        // When
        authProperties.setPublicPaths(singlePath);

        // Then
        assertEquals(1, authProperties.getPublicPaths().size());
        assertEquals("/api/public", authProperties.getPublicPaths().get(0));
    }

    @Test
    void publicPaths_ShouldHandlePathsWithSpecialCharacters() {
        // Given
        List<String> paths = Arrays.asList(
            "/api/member-service/login",
            "/api/product_service/public",
            "/api/cart.service/health"
        );

        // When
        authProperties.setPublicPaths(paths);

        // Then
        assertEquals(3, authProperties.getPublicPaths().size());
        assertTrue(authProperties.getPublicPaths().containsAll(paths));
    }
}

