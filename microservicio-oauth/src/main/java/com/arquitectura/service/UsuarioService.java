package com.arquitectura.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.arquitectura.clients.UsuarioFeignClient;
import com.arquitectura.usuario.entity.Usuario;

@Service
public class UsuarioService implements UserDetailsService,IUsuarioService{

	@Autowired
	private UsuarioFeignClient client;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		Usuario usuario = client.getUsuarioPorCorreo(username);
		
		if(usuario==null) {
			throw new UsernameNotFoundException("Error en el login, no existe el usuario");
		}
		List<GrantedAuthority> authorities = usuario.getRoles()
				.stream()
				.map(role -> new SimpleGrantedAuthority(role.getNombre()))				
				.collect(Collectors.toList());
		return new User(usuario.getCorreo(), usuario.getContrasena(), usuario.isEnabled(),true,true,true,authorities);
	}

	@Override
	public Usuario findByCorreo(String pCorreo) {
		
		Usuario usuario = client.getUsuarioPorCorreo(pCorreo);
		return usuario;
	}

}
