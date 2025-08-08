package com.arquitectura;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationManagerJwt implements AuthenticationManager {

    @SuppressWarnings("unchecked")
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // Obtenemos el token JWT del objeto Authentication
        String token = authentication.getCredentials().toString();

        // Obtenemos la clave pública para validar el token
        PublicKey publicKey = getPublicKey();

        // Parseamos el token y obtenemos los claims
        var claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Extraemos el nombre de usuario y roles del token
        String username = claims.get("user_name", String.class);
        List<String> roles = claims.get("authorities", List.class);

        // Convertimos los roles a GrantedAuthority
        Collection<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // Retornamos un objeto de autenticación con el usuario y sus roles
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    private PublicKey getPublicKey() {
        try {
            // Clave pública RSA
            String rsaPublicKey = JwtConfig.RSA_PUBLIC;

            // Creamos un KeyFactory con el algoritmo RSA
            KeyFactory kf = KeyFactory.getInstance("RSA");

            // Decodificamos la clave pública en formato X509
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getMimeDecoder()
                    .decode(rsaPublicKey.replaceAll("-----END PUBLIC KEY-----", "")
                                        .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                                        .replaceAll("\n", "")));

            // Generamos la clave pública
            return kf.generatePublic(keySpecX509);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error al obtener la clave pública RSA", e);
        }
    }
}
