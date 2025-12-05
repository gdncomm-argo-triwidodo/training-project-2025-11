package com.blibli.training.gateway.filter;

import com.blibli.training.framework.security.JwtUtils;
import com.blibli.training.gateway.config.AuthProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthProperties authProperties;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    private List<String> publicPaths;

    @BeforeEach
    void setUp() {
        publicPaths = Arrays.asList("/api/member/register", "/api/member/login", "/public");
        lenient().when(authProperties.getPublicPaths()).thenReturn(publicPaths);
    }

    @Test
    void filter_WithPublicPath_ShouldAllowRequest() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/member/register")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        verify(chain, times(1)).filter(exchange);
        verify(jwtUtils, never()).validateToken(anyString());
    }

    @Test
    void filter_WithPublicPathPrefix_ShouldAllowRequest() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/public/products")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        verify(chain, times(1)).filter(exchange);
        verify(jwtUtils, never()).validateToken(anyString());
    }

    @Test
    void filter_WithValidTokenInHeader_ShouldAllowRequestAndAddUserId() {
        // Given
        String token = "valid.jwt.token";
        Long userId = 123L;
        
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getClaimFromToken(eq(token), any())).thenReturn(userId);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(jwtUtils, times(1)).validateToken(token);
        verify(jwtUtils, times(1)).getClaimFromToken(eq(token), any());
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_WithValidTokenInCookie_ShouldAllowRequestAndAddUserId() {
        // Given
        String token = "valid.jwt.token";
        Long userId = 456L;
        
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/cart")
                .cookie(new HttpCookie("token", token))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getClaimFromToken(eq(token), any())).thenReturn(userId);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(jwtUtils, times(1)).validateToken(token);
        verify(jwtUtils, times(1)).getClaimFromToken(eq(token), any());
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_WithoutToken_ShouldReturnUnauthorized() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/cart")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
        verify(jwtUtils, never()).validateToken(anyString());
    }

    @Test
    void filter_WithInvalidToken_ShouldReturnUnauthorized() {
        // Given
        String token = "invalid.jwt.token";
        
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtUtils.validateToken(token)).thenReturn(false);

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtUtils, times(1)).validateToken(token);
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_WithMalformedAuthorizationHeader_ShouldReturnUnauthorized() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "InvalidFormat token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_HeaderTokenTakesPrecedenceOverCookie_ShouldUseHeaderToken() {
        // Given
        String headerToken = "header.jwt.token";
        String cookieToken = "cookie.jwt.token";
        Long userId = 789L;
        
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + headerToken)
                .cookie(new HttpCookie("token", cookieToken))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtUtils.validateToken(headerToken)).thenReturn(true);
        when(jwtUtils.getClaimFromToken(eq(headerToken), any())).thenReturn(userId);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(jwtUtils, times(1)).validateToken(headerToken);
        verify(jwtUtils, never()).validateToken(cookieToken);
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_WithNonPublicPath_RequiresAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void getOrder_ShouldReturnNegativeOne() {
        // When
        int order = authenticationFilter.getOrder();

        // Then
        assertEquals(-1, order);
    }

    @Test
    void filter_WithCaseInsensitivePublicPath_ShouldAllow() {
        // Given - public path is "/public"
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/Public")  // Different case
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        // Should allow since equalsIgnoreCase is used
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_WithExactPublicPathMatch_ShouldAllow() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/member/login")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void filter_WithValidToken_UserIdShouldBeExtractedFromClaims() {
        // Given
        String token = "valid.jwt.token";
        Long expectedUserId = 999L;
        
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getClaimFromToken(eq(token), any())).thenAnswer(invocation -> {
            Function<Claims, Object> resolver = invocation.getArgument(1);
            Claims mockClaims = mock(Claims.class);
            when(mockClaims.get("userId")).thenReturn(expectedUserId);
            return resolver.apply(mockClaims);
        });
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        Mono<Void> result = authenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(jwtUtils, times(1)).getClaimFromToken(eq(token), any());
    }
}

