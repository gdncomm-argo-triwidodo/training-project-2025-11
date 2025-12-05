package com.blibli.training.gateway.filter;

import com.blibli.training.framework.dto.BaseResponse;
import com.blibli.training.framework.exception.AuthenticationException;
import com.blibli.training.framework.security.JwtUtils;
import com.blibli.training.gateway.config.AuthProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final AuthProperties authProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        // for public
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // for private

        String token = extractToken(exchange.getRequest());
        if (token == null || !jwtUtils.validateToken(token)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            
            try {
                BaseResponse<String> errorResponse = BaseResponse.error(HttpStatus.UNAUTHORIZED.value(), new AuthenticationException("You don't have access to this page"));
                byte[] bytes = new ObjectMapper().writeValueAsBytes(errorResponse);
                DataBuffer buffer = response.bufferFactory().wrap(bytes);
                return response.writeWith(Mono.just(buffer));
            } catch (Exception e) {
                return response.setComplete();
            }
        }

        Object userIdObj = jwtUtils.getClaimFromToken(token, claims -> claims.get("userId"));
        String userId = String.valueOf(userIdObj);
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    private String extractToken(ServerHttpRequest request) {
        // Check Header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Check Cookie
        HttpCookie cookie = request.getCookies().getFirst("token");
        if (cookie != null) {
            return cookie.getValue();
        }

        return null;
    }

    private boolean isPublicPath(String path) {
        return authProperties.getPublicPaths().stream()
                .anyMatch(publicPath -> path.startsWith(publicPath) || path.equalsIgnoreCase(publicPath));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
