package com.arquitectura;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import io.jsonwebtoken.Jwts;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;


import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
public class AuthenticationManagerJwt implements ReactiveAuthenticationManager {

	@SuppressWarnings("unchecked")
	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {

		 return Mono.just(authentication.getCredentials().toString())
			        .map(token -> {
			            PublicKey publicKey = getPublicKey();
			            return Jwts.parserBuilder()
			                       .setSigningKey(publicKey)
			                       .build()
			                       .parseClaimsJws(token)
			                       .getBody();
			        })
			        .map(claims -> {
			            String username = claims.get("user_name", String.class);
			            List<String> roles = claims.get("authorities", List.class);
			            Collection<GrantedAuthority> authorities = roles.stream()
			                                                            .map(SimpleGrantedAuthority::new)
			                                                            .collect(Collectors.toList());
			            return new UsernamePasswordAuthenticationToken(username, null, authorities);
			        });
	}

	
	private PublicKey getPublicKey() {
	    try {
	        String rsaPublicKey = JwtConfig.RSA_PUBLIC;
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getMimeDecoder().decode(rsaPublicKey.replaceAll("-----END PUBLIC KEY-----", "").replaceAll("-----BEGIN PUBLIC KEY-----", "").replaceAll("\n", "")));
	        return kf.generatePublic(keySpecX509);
	    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
	        throw new RuntimeException("Error al obtener la clave pública RSA", e);
	    }
	}
}
