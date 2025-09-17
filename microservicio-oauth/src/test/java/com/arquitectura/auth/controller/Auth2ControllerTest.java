package com.arquitectura.auth.controller;

import com.arquitectura.auth.dto.*;
import com.arquitectura.auth.service.Auth2Service;
import com.arquitectura.service.UsuarioService;
import com.arquitectura.usuario.entity.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para Auth2Controller
 */
@ExtendWith(MockitoExtension.class)
class Auth2ControllerTest {

    @Mock
    private Auth2Service auth2Service;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private Auth2Controller auth2Controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auth2Controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testWebLoginWithGoogleIdToken_Success() throws Exception {
        // Given
        String idToken = "valid-google-id-token";
        GoogleUserInfo googleUserInfo = new GoogleUserInfo();
        googleUserInfo.setSub("google-user-id");
        googleUserInfo.setEmail("test@example.com");

        Usuario usuario = new Usuario();
        usuario.setNumeroDocumento("12345678");
        usuario.setCorreo("test@example.com");

        JwtTokenResponse jwtResponse = new JwtTokenResponse(
                "jwt-token", "bearer", 3600, "read write",
                "12345678", "test@example.com", "Test User", null
        );

        when(auth2Service.validateGoogleIdToken(idToken)).thenReturn(googleUserInfo);
        when(usuarioService.getUsuarioByProviderId("google-user-id", 0)).thenReturn(usuario);
        when(auth2Service.generateJwtToken(usuario, "test@example.com")).thenReturn(jwtResponse);

        // When & Then
        mockMvc.perform(post("/auth2/web/google-login")
                        .param("idToken", idToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("jwt-token"))
                .andExpect(jsonPath("$.token_type").value("bearer"));

        verify(auth2Service).validateGoogleIdToken(idToken);
        verify(usuarioService).getUsuarioByProviderId("google-user-id", 0);
        verify(auth2Service).generateJwtToken(usuario, "test@example.com");
    }

    @Test
    void testWebLoginWithGoogleIdToken_InvalidToken() throws Exception {
        // Given
        String idToken = "invalid-google-id-token";
        when(auth2Service.validateGoogleIdToken(idToken)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/auth2/web/google-login")
                        .param("idToken", idToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token de Google inválido"));

        verify(auth2Service).validateGoogleIdToken(idToken);
        verify(usuarioService, never()).getUsuarioByProviderId(anyString(), anyInt());
    }

    @Test
    void testWebLoginWithGoogleIdToken_UserNotFound() throws Exception {
        // Given
        String idToken = "valid-google-id-token";
        GoogleUserInfo googleUserInfo = new GoogleUserInfo();
        googleUserInfo.setSub("google-user-id");
        googleUserInfo.setEmail("test@example.com");

        when(auth2Service.validateGoogleIdToken(idToken)).thenReturn(googleUserInfo);
        when(usuarioService.getUsuarioByProviderId("google-user-id", 0)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/auth2/web/google-login")
                        .param("idToken", idToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado con ese googleId"));

        verify(auth2Service).validateGoogleIdToken(idToken);
        verify(usuarioService).getUsuarioByProviderId("google-user-id", 0);
        verify(auth2Service, never()).generateJwtToken(any(), anyString());
    }

    @Test
    void testWebLoginWithGoogleIdToken_NullIdToken() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth2/web/google-login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("idToken es requerido"));

        verify(auth2Service, never()).validateGoogleIdToken(anyString());
    }

    @Test
    void testValidateRegistrationToken_Success() throws Exception {
        // Given
        String regToken = "valid-registration-token";
        RegistrationTokenData tokenData = new RegistrationTokenData();
        tokenData.setCorreo("test@example.com");
        tokenData.setAction("REGISTER");

        when(auth2Service.validateAndDecodeRegistrationToken(regToken)).thenReturn(tokenData);

        // When & Then
        mockMvc.perform(post("/auth2/validate-registration-token")
                        .param("regToken", regToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("test@example.com"))
                .andExpect(jsonPath("$.action").value("REGISTER"));

        verify(auth2Service).validateAndDecodeRegistrationToken(regToken);
    }

    @Test
    void testValidateLoginToken_Success() throws Exception {
        // Given
        String regToken = "valid-login-token";
        RegistrationTokenData tokenData = new RegistrationTokenData();
        tokenData.setCorreo("test@example.com");
        tokenData.setAction("AUTO_LOGIN");

        when(auth2Service.validateAndDecodeRegistrationToken(regToken)).thenReturn(tokenData);

        // When & Then
        mockMvc.perform(post("/auth2/validate-login-token")
                        .param("regToken", regToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("test@example.com"))
                .andExpect(jsonPath("$.action").value("AUTO_LOGIN"));

        verify(auth2Service).validateAndDecodeRegistrationToken(regToken);
    }

    @Test
    void testValidateRegistrationToken_InvalidToken() throws Exception {
        // Given
        String regToken = "invalid-token";
        when(auth2Service.validateAndDecodeRegistrationToken(regToken))
                .thenThrow(new RuntimeException("JWT inválido o expirado"));

        // When & Then
        mockMvc.perform(post("/auth2/validate-registration-token")
                        .param("regToken", regToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token inválido o expirado: JWT inválido o expirado"));

        verify(auth2Service).validateAndDecodeRegistrationToken(regToken);
    }

    @Test
    void testGoogleCallback_RegisterAction() throws Exception {
        // Given
        String code = "google-auth-code";
        String state = "encoded-state";

        OAuth2CallbackResponse callbackResponse = new OAuth2CallbackResponse();
        callbackResponse.setAction("REGISTER");
        callbackResponse.setCorreo("test@example.com");

        when(auth2Service.processGoogleCallback(code, state)).thenReturn(callbackResponse);
        when(auth2Service.createTemporaryRegistrationToken(any())).thenReturn("temp-jwt-token");

        // When & Then
        mockMvc.perform(get("/login/auth2/google")
                        .param("code", code)
                        .param("state", state))
                .andExpect(status().is3xxRedirection());

        verify(auth2Service).processGoogleCallback(code, state);
        verify(auth2Service).createTemporaryRegistrationToken(any());
    }

    @Test
    void testGoogleCallback_AutoLoginAction() throws Exception {
        // Given
        String code = "google-auth-code";
        String state = "encoded-state";

        OAuth2CallbackResponse callbackResponse = new OAuth2CallbackResponse();
        callbackResponse.setAction("AUTO_LOGIN");
        callbackResponse.setCorreo("test@example.com");

        when(auth2Service.processGoogleCallback(code, state)).thenReturn(callbackResponse);
        when(auth2Service.createTemporaryRegistrationToken(any())).thenReturn("temp-jwt-token");

        // When & Then
        mockMvc.perform(get("/login/auth2/google")
                        .param("code", code)
                        .param("state", state))
                .andExpect(status().is3xxRedirection());

        verify(auth2Service).processGoogleCallback(code, state);
        verify(auth2Service).createTemporaryRegistrationToken(any());
    }

    @Test
    void testRedirectToGoogle() throws Exception {
        // Given
        String originalUrl = "/dashboard";

        // When & Then
        mockMvc.perform(get("/auth2/authorization/google")
                        .param("originalUrl", originalUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://accounts.google.com/o/oauth2/auth?*"));
    }

    @Test
    void testRedirectToGoogle_NoOriginalUrl() throws Exception {
        // When & Then
        mockMvc.perform(get("/auth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://accounts.google.com/o/oauth2/auth?*"));
    }
}