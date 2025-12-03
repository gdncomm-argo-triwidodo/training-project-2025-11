package com.blibli.training.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "gateway.auth")
public class AuthProperties {

    private List<String> publicPaths = new ArrayList<>();

}

