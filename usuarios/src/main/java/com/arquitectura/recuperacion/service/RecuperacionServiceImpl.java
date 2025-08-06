package com.arquitectura.recuperacion.service;


import com.arquitectura.mail.SendEmailAmazonService;
import com.arquitectura.recuperacion.entity.Recuperacion;
import com.arquitectura.recuperacion.entity.RecuperacionRepository;
import com.arquitectura.rol.entity.Role;
import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario.entity.UsuarioRepository;
import org.apache.kafka.common.Uuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecuperacionServiceImpl implements RecuperacionService{

	@Autowired
	private RecuperacionRepository repository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private SendEmailAmazonService emailService;
	
	@Override
	public Recuperacion crearRecuperacion(String correo) throws Exception {
		Recuperacion recuperacion = new Recuperacion();
		Usuario usuario = usuarioRepository.findByCorreo(correo);
		if(usuario!=null) {
			List<Role> roles = usuario.getRoles();
			boolean esCliente= false;
			for(int i =0; i<roles.size()&&!esCliente;i++) {
				if(roles.get(i).getNombre().equals("ROLE_CLIENTE")) {
					esCliente=true;
				}
			}
			if(esCliente) {
				recuperacion.setRecuperacionUsuario(usuario);
				recuperacion.setIdBusqueda(Uuid.randomUuid().toString());
				Recuperacion recuperacionBd = repository.save(recuperacion);
				emailService.mandarCorreoContrasenia(correo, recuperacion.getIdBusqueda());
				
				return recuperacionBd;
			}
			}
		
		return null;
	}

	@Override
	public Recuperacion getRecuperacion(String idBusqueda) {
		return repository.findByIdBusqueda(idBusqueda);
	}

	@Override
	public Recuperacion save(Recuperacion pRecuperacion) {
		return repository.save(pRecuperacion);
	}

}
