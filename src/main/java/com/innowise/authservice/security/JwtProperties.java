package com.innowise.authservice.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.security.jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;

    private Long accessTokenTtl;

    private Long refreshTokenTtl;
}