package com.arquitectura.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationTokenData {

    private String correo;
    private String nombre;
    private String googleId;
    private String facebookId;
    private String accessToken;
    private String refreshToken;
    private Boolean emailVerified;
    private String idToken;
    private String action;
    private String provider;
}