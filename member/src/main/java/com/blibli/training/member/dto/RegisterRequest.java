package com.blibli.training.member.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String password;
    private String email;
}
