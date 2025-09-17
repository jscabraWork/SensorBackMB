package com.arquitectura.auth.controller;

import com.arquitectura.auth.dto.*;
import com.arquitectura.auth.service.Auth2Service;
import com.arquitectura.service.UsuarioService;
import com.arquitectura.usuario.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controlador para autenticación OAuth2 con Google
 *
 * Maneja el flujo completo de autenticación OAuth2:
 * - Redirección a Google OAuth
 * - Callback de Google con código de autorización
 * - Login web directo con idToken de Google
 * - Validación de tokens de registro/login
 *
 * @author AllTickets
 * @version 1.0
 */
@RestController
public class Auth2Controller {

    @Autowired
    private Auth2Service auth2Service;

    @Autowired
    private UsuarioService usuarioService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${redirect-url}")
    private String redirectUrlSensor;

    @Value("${frontend-base-url}")
    private String frontendBaseUrlSensor;

    /**
     * Callback de Google OAuth2 - Recibe el código de autorización y procesa el login/registro
     *
     * Este endpoint es llamado por Google después de que el usuario autoriza la aplicación.
     * Procesa el código de autorización y determina si el usuario debe:
     * - Registrarse (nuevo usuario)
     * - Hacer login automático (usuario existente con Google asociado)
     * - Asociar Google a cuenta existente (usuario existe pero sin Google)
     *
     * @param code Código de autorización de Google
     * @param state Estado codificado que contiene la URL original
     * @param response HttpServletResponse para redirección
     * @throws IOException Si hay error en la redirección
     */
    @GetMapping("/login/auth2/google")
    public void googleCallback(@RequestParam("code") String code, @RequestParam("state") String state, HttpServletResponse response) throws IOException {
        try {
            // Decodificar el state para obtener la URL original
            String originalUrl = "/home"; // URL por defecto
            try {
                String decodedState = new String(java.util.Base64.getDecoder().decode(state));
                String[] stateParts = decodedState.split("\\|", 2);
                if (stateParts.length == 2) {
                    originalUrl = stateParts[1];
                }
            } catch (Exception e) {
                System.out.println("Error decodificando state, usando URL por defecto: " + e.getMessage());
            }

            OAuth2CallbackResponse googleData = auth2Service.processGoogleCallback(code, state);
            googleData.setProvider("google");
            googleData.setOriginalUrl(originalUrl); // Agregar la URL original a los datos

            String action = googleData.getAction();

            if ("REGISTER".equals(action)) {
                // Para registro: crear JWT temporal con todos los datos
                String tempJwt = auth2Service.createTemporaryRegistrationToken(googleData);

                // Para registro, siempre ir al login para completar el proceso
                String frontendUrl = redirectUrlSensor + "?" +
                        "regToken=" + tempJwt + "&originalUrl=" + java.net.URLEncoder.encode(originalUrl, "UTF-8");

                response.sendRedirect(frontendUrl);
            } else {
                String tempIdTokenJwt = auth2Service.createTemporaryRegistrationToken(googleData);

                // Para login, redirigir siempre a la originalUrl con el token
                String separator = originalUrl.contains("?") ? "&" : "?";
                String frontendUrl = frontendBaseUrlSensor + originalUrl + separator + "logToken=" + tempIdTokenJwt;
                response.sendRedirect(frontendUrl);
            }
        } catch (Exception e) {
            // Obtener la URL original del state si existe
            String originalUrl = "/home";
            try {
                String decodedState = new String(java.util.Base64.getDecoder().decode(state));
                String[] stateParts = decodedState.split("\\|", 2);
                if (stateParts.length == 2) {
                    originalUrl = stateParts[1];
                }
            } catch (Exception stateError) {
                System.out.println("Error decodificando state en catch: " + stateError.getMessage());
            }
            // Redirigir al frontend con error y URL original
            response.sendRedirect(redirectUrlSensor);
        }
    }

    /**
     * Inicia el flujo OAuth2 redirigiendo al usuario a Google
     *
     * Construye la URL de autorización de Google y redirige al usuario.
     * Preserva la URL original en el parámetro state para redirección posterior.
     *
     * @param originalUrl URL a la que redirigir después del login (opcional)
     * @param response HttpServletResponse para redirección
     * @throws IOException Si hay error en la redirección
     */
    @GetMapping("/auth2/authorization/google")
    public void redirectToGoogle(@RequestParam(required = false) String originalUrl, HttpServletResponse response) throws IOException {
        // Si no se proporciona originalUrl, usar una URL por defecto
        String urlToRedirect = (originalUrl != null && !originalUrl.isEmpty()) ? originalUrl : "/home";

        // Codificar la URL original en el state junto con un timestamp para seguridad
        String state = System.currentTimeMillis() + "|" + urlToRedirect;
        String encodedState = java.util.Base64.getEncoder().encodeToString(state.getBytes());

        String googleAuthUrl = "https://accounts.google.com/o/oauth2/auth?" +
                "response_type=code&" +
                "client_id=" + clientId + "&" +
                "scope=openid profile email&" +
                "redirect_uri=" + redirectUri + "&" +
                "state=" + encodedState;

        response.sendRedirect(googleAuthUrl);
    }

    /**
     * Login directo con idToken de Google
     *
     * Permite hacer login directamente con un idToken de Google válido.
     * Valida el token, busca al usuario en la base de datos y genera un JWT.
     * Usado principalmente para login automático después de asociar cuentas.
     *
     * @param idToken Token de ID de Google
     * @return JWT de sesión si el login es exitoso, error si no
     */
    @PostMapping("/auth2/web/google-login")
    public ResponseEntity<?> webLoginWithGoogleIdToken(@RequestParam String idToken) {

        try {
            if (idToken == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("idToken es requerido"));
            }

            // Validar idToken y obtener claims
            GoogleUserInfo googleUserInfo = auth2Service.validateGoogleIdToken(idToken);

            if (googleUserInfo == null) {
                return ResponseEntity.status(401).body(new ErrorResponse("Token de Google inválido"));
            }

            String googleId = googleUserInfo.getSub();
            String correo = googleUserInfo.getEmail();

            // Buscar usuario en BD usando googleId y tipoProvider = 0 (Google)
            Usuario usuario = usuarioService.getUsuarioByProviderId(googleId, 0);

            if (usuario != null) {
                // Generar JWT usando el servicio
                JwtTokenResponse jwtResponse = auth2Service.generateJwtToken(usuario, correo);
                return ResponseEntity.ok(jwtResponse);
            } else {
                return ResponseEntity.status(404).body(new ErrorResponse("Usuario no encontrado con ese googleId"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Error en login con Google: " + e.getMessage()));
        }
    }


    /**
     * Valida y decodifica tokens temporales de registro/login
     *
     * Valida tokens JWT temporales creados durante el flujo OAuth2.
     * Estos tokens contienen información del usuario de Google para
     * completar el registro o hacer login.
     *
     * @param regToken Token temporal de registro/login
     * @return Datos decodificados del token si es válido, error si no
     */
    @PostMapping({"/auth2/validate-registration-token", "/auth2/validate-login-token"})
    public ResponseEntity<?> validateRegistrationToken(@RequestParam String regToken) {

        try {
            RegistrationTokenData registrationData = auth2Service.validateAndDecodeRegistrationToken(regToken);
            return ResponseEntity.ok(registrationData);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Token inválido o expirado: " + e.getMessage()));
        }
    }

}
