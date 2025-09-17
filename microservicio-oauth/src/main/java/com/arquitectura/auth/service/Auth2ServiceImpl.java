package com.arquitectura.auth.service;

import com.arquitectura.auth.dto.*;
import com.arquitectura.service.UsuarioService;
import com.arquitectura.usuario.entity.Usuario;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de autenticación OAuth2 con Google
 *
 * Proporciona funcionalidades para:
 * - Intercambio de códigos de autorización por tokens
 * - Validación de tokens de Google
 * - Procesamiento del flujo OAuth2 completo
 * - Generación de JWT para sesiones de usuario
 * - Manejo de tokens temporales para registro/login
 *
 * @author AllTickets
 * @version 1.0
 */
@Service
public class Auth2ServiceImpl implements Auth2Service {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.jwt.registration.secret}")
    private String jwtSecretString;

    @Value("${oauth.jwt.registration.expiration-minutes:15}")
    private int jwtExpirationMinutes;

    /**
     * Intercambia el código de autorización de Google por tokens de acceso
     *
     * @param code Código de autorización obtenido del callback de Google
     * @return GoogleTokenResponse conteniendo access_token, refresh_token e id_token
     */
    @Override
    public GoogleTokenResponse exchangeCodeForTokenResponse(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        return restTemplate.postForObject(
                "https://oauth2.googleapis.com/token",
                new HttpEntity<>(params, headers),
                GoogleTokenResponse.class
        );
    }

    /**
     * Procesa el callback completo de Google OAuth2
     *
     * Determina qué acción debe tomar el frontend basado en el estado del usuario:
     * - REGISTER: Usuario nuevo, debe completar registro
     * - AUTO_LOGIN: Usuario existente con Google ya asociado
     * - ASSOCIATE_GOOGLE: Usuario existente sin Google asociado
     *
     * @param code Código de autorización de Google
     * @param state Estado codificado (opcional)
     * @return OAuth2CallbackResponse con la acción y datos necesarios
     */
    @Override
    public OAuth2CallbackResponse processGoogleCallback(String code, String state) {
        try {

            // 1. Intercambiar código por tokens
            GoogleTokenResponse tokenResponse = exchangeCodeForTokenResponse(code);
            String accessToken = tokenResponse.getAccessToken();
            String refreshToken = tokenResponse.getRefreshToken();
            String idToken = tokenResponse.getIdToken();

            // 2. Validar y obtener información del usuario desde el ID Token
            GoogleUserInfo userInfo = validateGoogleIdToken(idToken);
            if (userInfo == null) {
                throw new RuntimeException("ID Token de Google inválido");
            }

            String correo = userInfo.getEmail();
            String nombre = userInfo.getName();
            String googleId = userInfo.getSub(); // En ID Token 'sub' es 'id'

            // 3. Buscar usuario en base de datos
            Usuario usuario = usuarioService.findByCorreo(correo);

            // 4. Preparar respuesta
            OAuth2CallbackResponse response = new OAuth2CallbackResponse();
            response.setGoogleId(googleId);
            response.setCorreo(correo);
            response.setNombre(nombre);
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setIdToken(idToken);

            if (usuario != null) {
                // Verificar si el usuario ya tiene provider Google asociado
                try {
                    Usuario usuarioConProvider = usuarioService.getUsuarioByProviderId(googleId, 0);
                    if (usuarioConProvider != null) {
                        // VALIDACIÓN DE SEGURIDAD: Verificar que el usuario por correo sea el mismo que por Google ID
                        if (!usuario.getNumeroDocumento().equals(usuarioConProvider.getNumeroDocumento())) {
                            throw new RuntimeException("Conflicto de identidad: El correo " + correo +
                                    " está asociado a un usuario diferente al que tiene el Google ID " + googleId);
                        }

                        // Caso 1: Usuario con Google Provider existente (login automático)
                        response.setAction("AUTO_LOGIN");
                        response.setUser(usuario);
                        response.setMessage("Inicio de sesión automático con Google");
                    } else {
                        // Caso 2: Usuario sin Google Provider (asociar cuenta)
                        response.setAction("ASSOCIATE_GOOGLE");
                        response.setUser(usuario);
                        response.setMessage("Asociar cuenta Google con usuario existente");
                    }
                } catch (RuntimeException e) {
                    // Si es un error de conflicto de identidad, no permitir continuar
                    if (e.getMessage().contains("Conflicto de identidad")) {
                        throw e;
                    }
                    // No se encontró el provider, es asociación
                    response.setAction("ASSOCIATE_GOOGLE");
                    response.setUser(usuario);
                    response.setMessage("Asociar cuenta Google con usuario existente");
                }
            } else {
                // Caso 3: Nuevo registro
                response.setAction("REGISTER");
                OAuth2CallbackResponse.GoogleData googleData = new OAuth2CallbackResponse.GoogleData(
                        correo, nombre, googleId, true
                );
                response.setGoogleData(googleData);
                response.setMessage("Complete sus datos para registrarse");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error procesando OAuth callback", e);
        }
    }

    /**
     * Valida un ID Token de Google y extrae la información del usuario
     *
     * @param idToken Token de ID de Google a validar
     * @return GoogleUserInfo con datos del usuario o null si el token es inválido
     */
    @Override
    public GoogleUserInfo validateGoogleIdToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singleton(clientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken != null) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();
                GoogleUserInfo userInfo = new GoogleUserInfo();
                userInfo.setSub(payload.getSubject());
                userInfo.setEmail(payload.getEmail());
                userInfo.setName((String) payload.get("name"));
                userInfo.setPicture((String) payload.get("picture"));
                return userInfo;
            }
        } catch (Exception e) {
            System.err.println("Error validando token Google: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crea un JWT temporal con datos de OAuth para registro/login
     *
     * Este token se usa para transferir datos del OAuth callback al frontend
     * de forma segura y con expiración limitada.
     *
     * @param oauthData Datos del proceso OAuth2
     * @return JWT temporal codificado
     */
    @Override
    public String createTemporaryRegistrationToken(OAuth2CallbackResponse oauthData) {
        try {
            // Crear clave secreta desde el string Base64
            SecretKey jwtSecretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecretString));

            // Crear claims con los datos de Google
            Map<String, Object> claims = new HashMap<>();
            claims.put("correo", oauthData.getCorreo());
            claims.put("nombre", oauthData.getNombre());

            // Para Google
            claims.put("googleId", oauthData.getGoogleId());
            claims.put("idToken", oauthData.getIdToken());
            claims.put("refreshToken", oauthData.getRefreshToken());

            claims.put("accessToken", oauthData.getAccessToken());
            claims.put("emailVerified", true);
            claims.put("action", oauthData.getAction());
            claims.put("type", "registration");

            claims.put("provider", oauthData.getProvider());

            // Crear JWT con expiración configurable
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject("oauth-registration")
                    .setIssuedAt(new Date())
                    .setExpiration(Date.from(Instant.now().plus(jwtExpirationMinutes, ChronoUnit.MINUTES)))
                    .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            throw new RuntimeException("Error creando JWT temporal: " + e.getMessage());
        }
    }

    /**
     * Genera un JWT de sesión para el usuario (8 horas de duración)
     *
     * @param usuario Usuario para el que generar el token
     * @param correo Correo del usuario
     * @return JwtTokenResponse con el token de sesión
     */
    @Override
    public JwtTokenResponse generateJwtToken(Usuario usuario, String correo) {
        // Metodo original mantiene 8 horas (28800 segundos) - NO CAMBIAR para compatibilidad con WEB
        return generateJwtTokenForUser(usuario, correo, 28800);
    }

    public JwtTokenResponse generateJwtTokenForUser(Usuario usuario, String correo, int expirationSeconds) {
        try {
            // Crear UserDetails con los roles del usuario
            List<GrantedAuthority> authorities = usuario.getRoles()
                    .stream()
                    .map(role -> new SimpleGrantedAuthority(role.getNombre()))
                    .collect(Collectors.toList());

            UserDetails userDetails = new User(correo, "", authorities);

            // Crear Authentication
            Authentication userAuth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, authorities);

            // Crear OAuth2Request (simular cliente OAuth2)
            OAuth2Request oAuth2Request = new OAuth2Request(
                    Map.of(), // requestParameters
                    "alltickets.front", // clientId
                    authorities, // authorities
                    true, // approved
                    Set.of("read", "write"), // scope
                    Set.of(), // resourceIds
                    null, // redirectUri
                    Set.of(), // responseTypes
                    Map.of() // extensionProperties
            );

            // Crear OAuth2Authentication
            OAuth2Authentication oAuth2Auth = new OAuth2Authentication(oAuth2Request, userAuth);

            // Generar JWT token con UUID único
            DefaultOAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(java.util.UUID.randomUUID().toString());

            // Configurar expiración dinámica
            accessToken.setExpiration(Date.from(Instant.now().plusSeconds(expirationSeconds)));

            // Agregar información adicional al token
            Map<String, Object> additionalInfo = Map.of(
                    "cc", usuario.getNumeroDocumento(),
                    "user_name", correo,
                    "nombre", usuario.getNombre(),
                    "scope", Set.of("read", "write"),
                    "authorities", authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())
            );
            accessToken.setAdditionalInformation(additionalInfo);

            // Convertir a JWT
            OAuth2AccessToken jwtToken = jwtAccessTokenConverter.enhance(accessToken, oAuth2Auth);

            List<String> authoritiesList = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return new JwtTokenResponse(
                    jwtToken.getValue(),
                    "bearer",
                    jwtToken.getExpiresIn(),
                    "read write",
                    usuario.getNumeroDocumento(),
                    correo,
                    usuario.getNombre(),
                    authoritiesList
            );

        } catch (Exception e) {
            throw new RuntimeException("Error generando JWT: " + e.getMessage());
        }
    }

    /**
     * Valida y decodifica un JWT temporal de registro/login
     *
     * @param tempJwt JWT temporal a validar
     * @return RegistrationTokenData con los datos decodificados
     * @throws RuntimeException si el token es inválido o ha expirado
     */
    @Override
    public RegistrationTokenData validateAndDecodeRegistrationToken(String tempJwt) {
        try {
            // Crear clave secreta desde el string Base64
            SecretKey jwtSecretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecretString));

            // Validar y decodificar JWT
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(tempJwt)
                    .getBody();

            // Verificar que sea un token de registro
            if (!"registration".equals(claims.get("type"))) {
                throw new RuntimeException("Token no es de tipo registro");
            }

            // Convertir claims a RegistrationTokenData
            RegistrationTokenData registrationData = new RegistrationTokenData();
            registrationData.setCorreo((String) claims.get("correo"));
            registrationData.setNombre((String) claims.get("nombre"));
            registrationData.setGoogleId((String) claims.get("googleId"));
            registrationData.setFacebookId((String) claims.get("facebookId"));
            registrationData.setAccessToken((String) claims.get("accessToken"));
            registrationData.setRefreshToken((String) claims.get("refreshToken"));
            registrationData.setEmailVerified((Boolean) claims.get("emailVerified"));
            registrationData.setIdToken((String) claims.get("idToken"));
            registrationData.setAction((String) claims.get("action"));
            registrationData.setProvider((String) claims.get("provider"));

            return registrationData;

        } catch (Exception e) {
            throw new RuntimeException("JWT inválido o expirado: " + e.getMessage());
        }
    }
}
