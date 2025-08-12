package com.arquitectura.security;


import java.util.HashMap;
import java.util.Map;

import com.arquitectura.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;
import com.arquitectura.usuario.entity.Usuario;


@Component
public class InfoAdicionalToken implements TokenEnhancer{

	@Autowired
	private UsuarioService usuarioService;
	
	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		
		Usuario usuario = usuarioService.findByCorreo(authentication.getName());
		
		Map<String, Object> info = new HashMap<>();
		info.put("cc", usuario.getNumeroDocumento());
		info.put("nombre", usuario.getNombre());
		
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
		return accessToken;
	}

}
