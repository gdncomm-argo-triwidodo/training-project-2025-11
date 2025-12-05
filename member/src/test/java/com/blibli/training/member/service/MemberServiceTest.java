package com.blibli.training.member.service;

import com.blibli.training.framework.exception.AuthenticationException;
import com.blibli.training.framework.security.JwtUtils;
import com.blibli.training.member.dto.LoginRequest;
import com.blibli.training.member.dto.LoginResponse;
import com.blibli.training.member.dto.RegisterRequest;
import com.blibli.training.member.entity.Member;
import com.blibli.training.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

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
    }

    @Test
    void register_WithNewEmail_ShouldCreateMember() {
        // Given
        when(memberRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // When
        Member result = memberService.register(registerRequest);

        // Then
        assertNotNull(result);
        verify(memberRepository, times(1)).findByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Given
        when(memberRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(testMember));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> memberService.register(registerRequest));
        assertEquals("Email already exists", exception.getMessage());
        verify(memberRepository, times(1)).findByEmail(registerRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void register_ShouldEncodePassword() {
        // Given
        String rawPassword = "mySecretPassword";
        String encodedPassword = "$2a$10$encodedSecretPassword";
        
        RegisterRequest request = new RegisterRequest();
        request.setEmail("secure@example.com");
        request.setPassword(rawPassword);

        Member savedMember = Member.builder()
                .id(2L)
                .email("secure@example.com")
                .password(encodedPassword)
                .build();

        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            assertEquals(encodedPassword, member.getPassword());
            return savedMember;
        });

        // When
        Member result = memberService.register(request);

        // Then
        assertNotNull(result);
        assertEquals(encodedPassword, result.getPassword());
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        // Given
        String token = "jwt.token.here";
        when(memberRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(loginRequest.getPassword(), testMember.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(anyString(), anyMap())).thenReturn(token);

        // When
        LoginResponse result = memberService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(token, result.getToken());
        assertEquals(testMember.getId(), result.getUserId());
        verify(memberRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testMember.getPassword());
        verify(jwtUtils, times(1)).generateToken(anyString(), anyMap());
    }

    @Test
    void login_WithInvalidEmail_ShouldThrowAuthenticationException() {
        // Given
        when(memberRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> memberService.login(loginRequest));
        assertEquals("Invalid username or password", exception.getMessage());
        verify(memberRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtils, never()).generateToken(anyString(), anyMap());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowAuthenticationException() {
        // Given
        when(memberRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(loginRequest.getPassword(), testMember.getPassword())).thenReturn(false);

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> memberService.login(loginRequest));
        assertEquals("Invalid username or password", exception.getMessage());
        verify(memberRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testMember.getPassword());
        verify(jwtUtils, never()).generateToken(anyString(), anyMap());
    }

    @Test
    void login_ShouldIncludeEmailAndUserIdInToken() {
        // Given
        String token = "jwt.token.here";
        when(memberRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(loginRequest.getPassword(), testMember.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(anyString(), anyMap())).thenAnswer(invocation -> {
            Map<String, Object> claims = invocation.getArgument(1);
            assertTrue(claims.containsKey("email"));
            assertTrue(claims.containsKey("userId"));
            assertEquals(testMember.getEmail(), claims.get("email"));
            assertEquals(testMember.getId(), claims.get("userId"));
            return token;
        });

        // When
        LoginResponse result = memberService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(token, result.getToken());
    }

    @Test
    void login_ShouldUseEmailAsSubject() {
        // Given
        String token = "jwt.token.here";
        when(memberRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(loginRequest.getPassword(), testMember.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(eq(testMember.getEmail()), anyMap())).thenReturn(token);

        // When
        LoginResponse result = memberService.login(loginRequest);

        // Then
        assertNotNull(result);
        verify(jwtUtils, times(1)).generateToken(eq(testMember.getEmail()), anyMap());
    }

    @Test
    void login_WithDifferentUsers_ShouldReturnDifferentUserIds() {
        // Given
        Member anotherMember = Member.builder()
                .id(2L)
                .email("another@example.com")
                .password("$2a$10$anotherEncodedPassword")
                .build();

        LoginRequest anotherLoginRequest = new LoginRequest();
        anotherLoginRequest.setEmail("another@example.com");
        anotherLoginRequest.setPassword("password456");

        String token1 = "jwt.token.user1";
        String token2 = "jwt.token.user2";

        when(memberRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testMember));
        when(memberRepository.findByEmail(anotherLoginRequest.getEmail())).thenReturn(Optional.of(anotherMember));
        when(passwordEncoder.matches(loginRequest.getPassword(), testMember.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(anotherLoginRequest.getPassword(), anotherMember.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(eq(testMember.getEmail()), anyMap())).thenReturn(token1);
        when(jwtUtils.generateToken(eq(anotherMember.getEmail()), anyMap())).thenReturn(token2);

        // When
        LoginResponse result1 = memberService.login(loginRequest);
        LoginResponse result2 = memberService.login(anotherLoginRequest);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(testMember.getId(), result1.getUserId());
        assertEquals(anotherMember.getId(), result2.getUserId());
        assertNotEquals(result1.getUserId(), result2.getUserId());
    }
}

