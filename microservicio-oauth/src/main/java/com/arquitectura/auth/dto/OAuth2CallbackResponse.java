package com.arquitectura.auth.dto;

import com.arquitectura.usuario.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2CallbackResponse {

    private String googleId;
    private String correo;
    private String nombre;
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private String action;
    private Usuario user;
    private GoogleData googleData;
    private String message;
    private String provider;
    private String originalUrl;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleData {
        private String correo;
        private String nombre;
        private String googleId;
        private Boolean emailVerified;
    }
}