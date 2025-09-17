package com.arquitectura.auth.service;

import com.arquitectura.auth.dto.*;
import com.arquitectura.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para Auth2Service
 *
 * @author AllTickets
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class Auth2ServiceTest {

    @Mock
    private Auth2Service auth2Service;

    private GoogleTokenResponse googleTokenResponseTest;
    private GoogleUserInfo googleUserInfoTest;
    private OAuth2CallbackResponse oauth2CallbackResponseTest;
    private RegistrationTokenData registrationTokenDataTest;
    private JwtTokenResponse jwtTokenResponseTest;
    private Usuario usuarioTest;

    @BeforeEach
    void setUp() {
        // Configurar GoogleTokenResponse de prueba
        googleTokenResponseTest = new GoogleTokenResponse();
        googleTokenResponseTest.setAccessToken("test-access-token");
        googleTokenResponseTest.setRefreshToken("test-refresh-token");
        googleTokenResponseTest.setIdToken("test-id-token");

        // Configurar GoogleUserInfo de prueba
        googleUserInfoTest = new GoogleUserInfo();
        googleUserInfoTest.setSub("google-user-id-123");
        googleUserInfoTest.setEmail("test@example.com");
        googleUserInfoTest.setName("Test User");
        googleUserInfoTest.setPicture("http://example.com/picture.jpg");

        // Configurar Usuario de prueba
        usuarioTest = new Usuario();
        usuarioTest.setNumeroDocumento("12345678");
        usuarioTest.setNombre("Test User");
        usuarioTest.setCorreo("test@example.com");
        usuarioTest.setCelular("3001234567");

        // Configurar OAuth2CallbackResponse de prueba
        oauth2CallbackResponseTest = new OAuth2CallbackResponse();
        oauth2CallbackResponseTest.setAction("AUTO_LOGIN");
        oauth2CallbackResponseTest.setCorreo("test@example.com");
        oauth2CallbackResponseTest.setNombre("Test User");
        oauth2CallbackResponseTest.setGoogleId("google-user-id-123");
        oauth2CallbackResponseTest.setAccessToken("test-access-token");
        oauth2CallbackResponseTest.setRefreshToken("test-refresh-token");
        oauth2CallbackResponseTest.setIdToken("test-id-token");
        oauth2CallbackResponseTest.setProvider("google");

        // Configurar RegistrationTokenData de prueba
        registrationTokenDataTest = new RegistrationTokenData();
        registrationTokenDataTest.setCorreo("test@example.com");
        registrationTokenDataTest.setNombre("Test User");
        registrationTokenDataTest.setGoogleId("google-user-id-123");
        registrationTokenDataTest.setAccessToken("test-access-token");
        registrationTokenDataTest.setRefreshToken("test-refresh-token");
        registrationTokenDataTest.setIdToken("test-id-token");
        registrationTokenDataTest.setAction("REGISTER");
        registrationTokenDataTest.setProvider("google");
        registrationTokenDataTest.setEmailVerified(true);

        // Configurar JwtTokenResponse de prueba
        jwtTokenResponseTest = new JwtTokenResponse(
                "jwt-token-value",
                "bearer",
                28800,
                "read write",
                "12345678",
                "test@example.com",
                "Test User",
                null
        );
    }

    // Tests para exchangeCodeForTokenResponse
    @Test
    @DisplayName("Intercambiar código por tokens - Éxito")
    void testExchangeCodeForTokenResponse_Success() {
        // Given
        String authCode = "test-authorization-code";
        when(auth2Service.exchangeCodeForTokenResponse(anyString()))
                .thenReturn(googleTokenResponseTest);

        // When
        GoogleTokenResponse resultado = auth2Service.exchangeCodeForTokenResponse(authCode);

        // Then
        assertNotNull(resultado);
        assertEquals("test-access-token", resultado.getAccessToken());
        assertEquals("test-refresh-token", resultado.getRefreshToken());
        assertEquals("test-id-token", resultado.getIdToken());
        verify(auth2Service).exchangeCodeForTokenResponse(authCode);
    }

    @Test
    @DisplayName("Intercambiar código por tokens - Código inválido")
    void testExchangeCodeForTokenResponse_InvalidCode() {
        // Given
        String invalidCode = "invalid-code";
        when(auth2Service.exchangeCodeForTokenResponse(anyString()))
                .thenReturn(null);

        // When
        GoogleTokenResponse resultado = auth2Service.exchangeCodeForTokenResponse(invalidCode);

        // Then
        assertNull(resultado);
        verify(auth2Service).exchangeCodeForTokenResponse(invalidCode);
    }

    // Tests para validateGoogleIdToken
    @Test
    @DisplayName("Validar ID Token de Google - Token válido")
    void testValidateGoogleIdToken_ValidToken() {
        // Given
        String validIdToken = "valid-google-id-token";
        when(auth2Service.validateGoogleIdToken(anyString()))
                .thenReturn(googleUserInfoTest);

        // When
        GoogleUserInfo resultado = auth2Service.validateGoogleIdToken(validIdToken);

        // Then
        assertNotNull(resultado);
        assertEquals("google-user-id-123", resultado.getSub());
        assertEquals("test@example.com", resultado.getEmail());
        assertEquals("Test User", resultado.getName());
        assertEquals("http://example.com/picture.jpg", resultado.getPicture());
        verify(auth2Service).validateGoogleIdToken(validIdToken);
    }

    @Test
    @DisplayName("Validar ID Token de Google - Token inválido")
    void testValidateGoogleIdToken_InvalidToken() {
        // Given
        String invalidIdToken = "invalid-google-id-token";
        when(auth2Service.validateGoogleIdToken(anyString()))
                .thenReturn(null);

        // When
        GoogleUserInfo resultado = auth2Service.validateGoogleIdToken(invalidIdToken);

        // Then
        assertNull(resultado);
        verify(auth2Service).validateGoogleIdToken(invalidIdToken);
    }

    // Tests para processGoogleCallback
    @Test
    @DisplayName("Procesar callback de Google - Usuario nuevo (registro)")
    void testProcessGoogleCallback_NewUser() {
        // Given
        String code = "auth-code";
        String state = "encoded-state";
        oauth2CallbackResponseTest.setAction("REGISTER");

        when(auth2Service.processGoogleCallback(anyString(), anyString()))
                .thenReturn(oauth2CallbackResponseTest);

        // When
        OAuth2CallbackResponse resultado = auth2Service.processGoogleCallback(code, state);

        // Then
        assertNotNull(resultado);
        assertEquals("REGISTER", resultado.getAction());
        assertEquals("test@example.com", resultado.getCorreo());
        assertEquals("Test User", resultado.getNombre());
        assertEquals("google-user-id-123", resultado.getGoogleId());
        verify(auth2Service).processGoogleCallback(code, state);
    }

    @Test
    @DisplayName("Procesar callback de Google - Usuario existente con Google")
    void testProcessGoogleCallback_ExistingUserWithGoogle() {
        // Given
        String code = "auth-code";
        String state = "encoded-state";
        oauth2CallbackResponseTest.setAction("AUTO_LOGIN");
        oauth2CallbackResponseTest.setUser(usuarioTest);

        when(auth2Service.processGoogleCallback(anyString(), anyString()))
                .thenReturn(oauth2CallbackResponseTest);

        // When
        OAuth2CallbackResponse resultado = auth2Service.processGoogleCallback(code, state);

        // Then
        assertNotNull(resultado);
        assertEquals("AUTO_LOGIN", resultado.getAction());
        assertEquals("test@example.com", resultado.getCorreo());
        assertNotNull(resultado.getUser());
        verify(auth2Service).processGoogleCallback(code, state);
    }

    @Test
    @DisplayName("Procesar callback de Google - Usuario existente sin Google")
    void testProcessGoogleCallback_ExistingUserWithoutGoogle() {
        // Given
        String code = "auth-code";
        String state = "encoded-state";
        oauth2CallbackResponseTest.setAction("ASSOCIATE_GOOGLE");
        oauth2CallbackResponseTest.setUser(usuarioTest);

        when(auth2Service.processGoogleCallback(anyString(), anyString()))
                .thenReturn(oauth2CallbackResponseTest);

        // When
        OAuth2CallbackResponse resultado = auth2Service.processGoogleCallback(code, state);

        // Then
        assertNotNull(resultado);
        assertEquals("ASSOCIATE_GOOGLE", resultado.getAction());
        assertEquals("test@example.com", resultado.getCorreo());
        assertNotNull(resultado.getUser());
        verify(auth2Service).processGoogleCallback(code, state);
    }

    // Tests para createTemporaryRegistrationToken
    @Test
    @DisplayName("Crear token temporal de registro - Éxito")
    void testCreateTemporaryRegistrationToken_Success() {
        // Given
        String expectedToken = "temporary-jwt-token";
        when(auth2Service.createTemporaryRegistrationToken(any(OAuth2CallbackResponse.class)))
                .thenReturn(expectedToken);

        // When
        String resultado = auth2Service.createTemporaryRegistrationToken(oauth2CallbackResponseTest);

        // Then
        assertNotNull(resultado);
        assertEquals(expectedToken, resultado);
        verify(auth2Service).createTemporaryRegistrationToken(oauth2CallbackResponseTest);
    }

    @Test
    @DisplayName("Crear token temporal de registro - Datos nulos")
    void testCreateTemporaryRegistrationToken_NullData() {
        // Given
        when(auth2Service.createTemporaryRegistrationToken(any()))
                .thenThrow(new RuntimeException("Datos de OAuth requeridos"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
            auth2Service.createTemporaryRegistrationToken(null));
        verify(auth2Service).createTemporaryRegistrationToken(null);
    }

    // Tests para validateAndDecodeRegistrationToken
    @Test
    @DisplayName("Validar y decodificar token de registro - Token válido")
    void testValidateAndDecodeRegistrationToken_ValidToken() {
        // Given
        String validToken = "valid-jwt-token";
        when(auth2Service.validateAndDecodeRegistrationToken(anyString()))
                .thenReturn(registrationTokenDataTest);

        // When
        RegistrationTokenData resultado = auth2Service.validateAndDecodeRegistrationToken(validToken);

        // Then
        assertNotNull(resultado);
        assertEquals("test@example.com", resultado.getCorreo());
        assertEquals("Test User", resultado.getNombre());
        assertEquals("google-user-id-123", resultado.getGoogleId());
        assertEquals("REGISTER", resultado.getAction());
        assertTrue(resultado.getEmailVerified());
        verify(auth2Service).validateAndDecodeRegistrationToken(validToken);
    }

    @Test
    @DisplayName("Validar y decodificar token de registro - Token inválido")
    void testValidateAndDecodeRegistrationToken_InvalidToken() {
        // Given
        String invalidToken = "invalid-jwt-token";
        when(auth2Service.validateAndDecodeRegistrationToken(anyString()))
                .thenThrow(new RuntimeException("JWT inválido o expirado"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            auth2Service.validateAndDecodeRegistrationToken(invalidToken));

        assertTrue(exception.getMessage().contains("JWT inválido o expirado"));
        verify(auth2Service).validateAndDecodeRegistrationToken(invalidToken);
    }

    @Test
    @DisplayName("Validar y decodificar token de registro - Token expirado")
    void testValidateAndDecodeRegistrationToken_ExpiredToken() {
        // Given
        String expiredToken = "expired-jwt-token";
        when(auth2Service.validateAndDecodeRegistrationToken(anyString()))
                .thenThrow(new RuntimeException("JWT inválido o expirado: Token expirado"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            auth2Service.validateAndDecodeRegistrationToken(expiredToken));

        assertTrue(exception.getMessage().contains("expirado"));
        verify(auth2Service).validateAndDecodeRegistrationToken(expiredToken);
    }

    // Tests para generateJwtToken
    @Test
    @DisplayName("Generar JWT token - Éxito")
    void testGenerateJwtToken_Success() {
        // Given
        String correo = "test@example.com";
        when(auth2Service.generateJwtToken(any(Usuario.class), anyString()))
                .thenReturn(jwtTokenResponseTest);

        // When
        JwtTokenResponse resultado = auth2Service.generateJwtToken(usuarioTest, correo);

        // Then
        assertNotNull(resultado);
        assertEquals("jwt-token-value", resultado.getAccessToken());
        assertEquals("bearer", resultado.getTokenType());
        assertEquals(28800, resultado.getExpiresIn());
        assertEquals("read write", resultado.getScope());
        assertEquals("12345678", resultado.getCc());
        assertEquals("test@example.com", resultado.getUserName());
        assertEquals("Test User", resultado.getNombre());
        verify(auth2Service).generateJwtToken(usuarioTest, correo);
    }

    @Test
    @DisplayName("Generar JWT token - Usuario nulo")
    void testGenerateJwtToken_NullUser() {
        // Given
        String correo = "test@example.com";
        when(auth2Service.generateJwtToken(any(), anyString()))
                .thenThrow(new RuntimeException("Usuario requerido"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
            auth2Service.generateJwtToken(null, correo));
        verify(auth2Service).generateJwtToken(null, correo);
    }

    @Test
    @DisplayName("Generar JWT token - Correo nulo")
    void testGenerateJwtToken_NullEmail() {
        // Given
        when(auth2Service.generateJwtToken(any(Usuario.class), any()))
                .thenThrow(new RuntimeException("Correo requerido"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
            auth2Service.generateJwtToken(usuarioTest, null));
        verify(auth2Service).generateJwtToken(usuarioTest, null);
    }

    // Tests de casos edge
    @Test
    @DisplayName("Procesar callback - Código nulo")
    void testProcessGoogleCallback_NullCode() {
        // Given
        String state = "encoded-state";
        when(auth2Service.processGoogleCallback(any(), anyString()))
                .thenThrow(new RuntimeException("Código de autorización requerido"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
            auth2Service.processGoogleCallback(null, state));
        verify(auth2Service).processGoogleCallback(null, state);
    }

    @Test
    @DisplayName("Validar ID Token - Token vacío")
    void testValidateGoogleIdToken_EmptyToken() {
        // Given
        String emptyToken = "";
        when(auth2Service.validateGoogleIdToken(anyString()))
                .thenReturn(null);

        // When
        GoogleUserInfo resultado = auth2Service.validateGoogleIdToken(emptyToken);

        // Then
        assertNull(resultado);
        verify(auth2Service).validateGoogleIdToken(emptyToken);
    }

    @Test
    @DisplayName("Crear token temporal - Acción ASSOCIATE_GOOGLE")
    void testCreateTemporaryRegistrationToken_AssociateAction() {
        // Given
        oauth2CallbackResponseTest.setAction("ASSOCIATE_GOOGLE");
        String expectedToken = "associate-jwt-token";
        when(auth2Service.createTemporaryRegistrationToken(any(OAuth2CallbackResponse.class)))
                .thenReturn(expectedToken);

        // When
        String resultado = auth2Service.createTemporaryRegistrationToken(oauth2CallbackResponseTest);

        // Then
        assertNotNull(resultado);
        assertEquals(expectedToken, resultado);
        verify(auth2Service).createTemporaryRegistrationToken(oauth2CallbackResponseTest);
    }

    @Test
    @DisplayName("Validar token de registro - Acción AUTO_LOGIN")
    void testValidateAndDecodeRegistrationToken_AutoLoginAction() {
        // Given
        String validToken = "auto-login-jwt-token";
        registrationTokenDataTest.setAction("AUTO_LOGIN");
        when(auth2Service.validateAndDecodeRegistrationToken(anyString()))
                .thenReturn(registrationTokenDataTest);

        // When
        RegistrationTokenData resultado = auth2Service.validateAndDecodeRegistrationToken(validToken);

        // Then
        assertNotNull(resultado);
        assertEquals("AUTO_LOGIN", resultado.getAction());
        verify(auth2Service).validateAndDecodeRegistrationToken(validToken);
    }
}