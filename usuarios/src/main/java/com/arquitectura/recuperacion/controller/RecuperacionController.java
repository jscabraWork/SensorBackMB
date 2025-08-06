package com.arquitectura.recuperacion.controller;

import com.arquitectura.recuperacion.entity.Recuperacion;
import com.arquitectura.recuperacion.service.RecuperacionService;
import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RecuperacionController {

	@Autowired
	private RecuperacionService service;
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@PostMapping("/recuperar-contrasena/{correo}")
	public ResponseEntity<?> recuperarContrasena(@PathVariable String correo) throws Exception{
		service.crearRecuperacion(correo);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	
	@GetMapping("/recuperar-contrasena/{pIdBusqueda}")
	public ResponseEntity<?> getRecuperarContrasena(@PathVariable String pIdBusqueda) throws Exception{
		Map<String, Object> response = new HashMap<>();
		Recuperacion recuperacion = service.getRecuperacion(pIdBusqueda);
		boolean valido=false;
		if(!recuperacion.isActivo()) {
			recuperacion=null;
		}
		if(recuperacion!=null) {
			valido=true;
		}
		response.put("valido", valido);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	
	
	@PutMapping("/recuperar-contrasena/{pIdBusqueda}")
	public ResponseEntity<?> cambioCOntrasena(@PathVariable String pIdBusqueda, @RequestBody String contrasena) throws Exception{
		Map<String, Object> response = new HashMap<>();
		Recuperacion recuperacion = service.getRecuperacion(pIdBusqueda);
		boolean valido=false;
		if(recuperacion.isActivo()) {
			Usuario usuario =recuperacion.getRecuperacionUsuario();
			usuario.setContrasena(passwordEncoder.encode(contrasena));
			valido=true;
			recuperacion.setActivo(false);
			usuarioService.save(usuario);
			service.save(recuperacion);
		}
		
		response.put("valido", valido);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
}
