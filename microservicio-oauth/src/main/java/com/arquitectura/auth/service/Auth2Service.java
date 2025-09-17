package com.arquitectura.auth.service;

import com.arquitectura.auth.dto.*;
import com.arquitectura.usuario.entity.Usuario;

public interface Auth2Service {

    /**
     * Intercambia el código de autorización por un token de acceso de Google
     */
    GoogleTokenResponse exchangeCodeForTokenResponse(String code);

    /**
     * Procesa el callback de Google OAuth y maneja login/registro
     */
    OAuth2CallbackResponse processGoogleCallback(String code, String state);

    /**
     * Valida el token de google
     */
    GoogleUserInfo validateGoogleIdToken (String idToken);

    /**
     * Crea un JWT temporal con los datos de registro de Google
     */
    String createTemporaryRegistrationToken(OAuth2CallbackResponse googleData);

    /**
     * Válida y decodifica un JWT temporal de registro
     */
    RegistrationTokenData validateAndDecodeRegistrationToken(String tempJwt);

    /**
     * Genera un JWT token para un usuario autenticado
     */
    JwtTokenResponse generateJwtToken(Usuario usuario, String correo);

}
