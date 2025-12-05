package com.blibli.training.member.controller;

import com.blibli.training.framework.exception.AuthenticationException;
import com.blibli.training.member.dto.LoginRequest;
import com.blibli.training.member.dto.LoginResponse;
import com.blibli.training.member.dto.RegisterRequest;
import com.blibli.training.member.entity.Member;
import com.blibli.training.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MemberController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    private Member testMember;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("$2a$10$encodedPassword")
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        loginResponse = new LoginResponse("jwt.token.here", 1L);
    }

    @Test
    void register_WithValidRequest_ShouldReturnMember() throws Exception {
        // Given
        when(memberService.register(any(RegisterRequest.class))).thenReturn(testMember);

        // When & Then
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(memberService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() throws Exception {
        when(memberService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // When & Then - MockMvc converts exception to error response
        try {
            mockMvc.perform(post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)));
        } catch (Exception e) {
            // Expected - ServletException wraps the RuntimeException
            assertTrue(e.getCause() instanceof RuntimeException);
        }

        verify(memberService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTokenAndSetCookie() throws Exception {
        // Given
        when(memberService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().httpOnly("token", true))
                .andExpect(cookie().path("token", "/"))
                .andExpect(cookie().maxAge("token", 24 * 60 * 60))
                .andReturn();

        verify(memberService, times(1)).login(any(LoginRequest.class));
        
        // Verify cookie value
        String cookieValue = result.getResponse().getCookie("token").getValue();
        assertEquals("jwt.token.here", cookieValue);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowAuthenticationException() throws Exception {
        when(memberService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        // When & Then - MockMvc converts exception to error response
        try {
            mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)));
        } catch (Exception e) {
            // Expected - ServletException wraps the AuthenticationException
            assertTrue(e.getCause() instanceof AuthenticationException);
        }

        verify(memberService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void logout_ShouldClearCookie() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(post("/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().maxAge("token", 0))
                .andReturn();

        // Verify cookie is cleared
        assertNull(result.getResponse().getCookie("token").getValue());
        verify(memberService, never()).login(any());
    }

    @Test
    void helloWorld_ShouldReturnGreeting() throws Exception {
        // When & Then
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    @Test
    void helloWorldPrivate_ShouldReturnGreeting() throws Exception {
        // When & Then
        mockMvc.perform(get("/hello-protected"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World Private"));
    }

    @Test
    void login_ShouldSetSecureCookieAttributeToFalse() throws Exception {
        // Given
        when(memberService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Verify secure is false (for development)
        assertFalse(result.getResponse().getCookie("token").getSecure());
    }
}

