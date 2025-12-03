package com.blibli.training.member.service;

import com.blibli.training.framework.exception.AuthenticationException;
import com.blibli.training.framework.security.JwtUtils;
import com.blibli.training.member.dto.LoginRequest;
import com.blibli.training.member.dto.LoginResponse;
import com.blibli.training.member.dto.RegisterRequest;
import com.blibli.training.member.entity.Member;
import com.blibli.training.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public Member register(RegisterRequest request) {
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Member member = Member.builder()
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        return memberRepository.save(member);
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", member.getEmail());
        claims.put("userId", member.getId());

        String token = jwtUtils.generateToken(String.valueOf(member.getEmail()), claims);
        return new LoginResponse(token, member.getId());
    }
}
