package com.arquitectura.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenResponse {

    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
    private String scope;
    private String cc;
    private String userName;
    private String nombre;
    private List<String> authorities;
}