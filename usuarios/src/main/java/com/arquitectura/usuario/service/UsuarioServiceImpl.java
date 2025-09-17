package com.arquitectura.usuario.service;

import com.arquitectura.clients.AuditoresFeignClient;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.rol.entity.Role;
import com.arquitectura.rol.entity.RoleRepository;
import com.arquitectura.services.CommonServiceImplString;
import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario.entity.UsuarioRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Uuid;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class UsuarioServiceImpl extends CommonServiceImplString< Usuario, UsuarioRepository> implements UsuarioService{

	@Autowired
	private RoleRepository roleRepository;

	@Value("${admins.topic}")
	private String adminsTopic;
	
	@Value("${clientes.topic}")
	private String clientesTopic;
	
	@Value("${coordinadores.topic}")
	private String coordinadoresTopic;
	
	@Value("${organizador.topic}")
	private String organizadorTopic;
	
	@Value("${promotor.topic}")
	private String promotorTopic;
	
	@Value("${puntoF.topic}")
	private String puntoFTopic;
	
	@Value("${contador.topic}")
	private String contadorTopic;

	@Autowired
	AuditoresFeignClient auditoresFeignClient;
	
	@Autowired
	private KafkaTemplate<String, Object>kafkaTemplate;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Transactional("transactionManager")
	@Override
	public Usuario crearUsuarioConMultiplesRoles(Usuario pUsuario, List<String> roles, boolean creando) {

		if(usuarioExiste(pUsuario) && creando) {
			return null;
		}

		// Mapa para asociar roles con topics y clases
		Map<String, RoleConfig> roleConfigMap = getStringRoleConfigMap();

		// Inicializar la lista de roles si es null
		if(pUsuario.getRoles() == null) {
			pUsuario.setRoles(new ArrayList<>());
		}

		// Agregar todos los roles al usuario
		for(String roleName : roles) {
			Role role = roleRepository.findByNombre(roleName);
			if(role != null && !pUsuario.getRoles().contains(role)) {
				pUsuario.agregarRole(role);
			}
		}

		// Si no es cliente y está creando, codificar contraseña
		if(!roles.contains("ROLE_CLIENTE") && creando) {
			pUsuario.setContrasena(passwordEncoder.encode(pUsuario.getContrasena()));
		}

		// Guardar primero el usuario
		Usuario usuarioGuardado = save(pUsuario);

		// Enviar eventos a Kafka para cada rol (excepto ROLE_CLIENTE)
		for(String roleName : roles) {
			RoleConfig config = roleConfigMap.get(roleName);
			enviarEventoKafka(config.topic(), config.clase(), usuarioGuardado);
		}

		return usuarioGuardado;
	}

	// Mapeo para asociar roles con topics y clases
	private Map<String, RoleConfig> getStringRoleConfigMap() {
		Map<String, RoleConfig> roleConfigMap = new HashMap<>();
		roleConfigMap.put("ROLE_CLIENTE", new RoleConfig(clientesTopic, "Cliente"));
		roleConfigMap.put("ROLE_ORGANIZADOR", new RoleConfig(organizadorTopic, "Organizador"));
		roleConfigMap.put("ROLE_PUNTO", new RoleConfig(puntoFTopic, "Punto"));
		roleConfigMap.put("ROLE_ADMIN", new RoleConfig(adminsTopic, "Admin"));
		roleConfigMap.put("ROLE_PROMOTOR", new RoleConfig(promotorTopic, "Promotor"));
		roleConfigMap.put("ROLE_COORDINADOR", new RoleConfig(coordinadoresTopic, "Coordinador"));
		roleConfigMap.put("ROLE_CONTADOR", new RoleConfig(contadorTopic, "Contador"));
		return roleConfigMap;
	}

	@Transactional("transactionManager")
	@Override
	public Usuario crearCliente(Usuario pUsuario, boolean creando){
		return crearUsuario(clientesTopic, pUsuario, "ROLE_CLIENTE", "Cliente",creando);
	}

	@Transactional("transactionManager")
	@Override
	public Usuario crearOrganizador(Usuario pUsuario, boolean creando) {
		return crearUsuario(organizadorTopic, pUsuario, "ROLE_ORGANIZADOR", "Organizador",creando);
	}

	@Transactional("transactionManager")
	@Override
	public Usuario crearPuntoFisico(Usuario pUsuario, boolean creando) {
		return crearUsuario(puntoFTopic, pUsuario, "ROLE_PUNTO", "Punto",creando);
	}

	@Transactional("transactionManager")
	@Override
	public Usuario crearAdministrador(Usuario pUsuario, boolean creando) {
		return crearUsuario(adminsTopic, pUsuario, "ROLE_ADMIN", "Admin",creando);
	}

	@Transactional("transactionManager")
	@Override
	public Usuario crearPromotor(Usuario pUsuario, boolean creando) {
		return crearUsuario(promotorTopic, pUsuario, "ROLE_PROMOTOR", "Promotor",creando);
	}
	
	@Transactional("transactionManager")
	@Override
	public Usuario crearCoordinador(Usuario pUsuario, boolean creando) {
		return crearUsuario(coordinadoresTopic, pUsuario, "ROLE_COORDINADOR", "Coordinador",creando);
	}

	@Transactional("transactionManager")
	@Override
	public Usuario crearContador(Usuario pUsuario, boolean creando) {
		return crearUsuario(contadorTopic, pUsuario, "ROLE_CONTADOR", "Contador",creando);
	}

	@Transactional("transactionManager")
	@Override
	public Usuario crearAuditor(Usuario pUsuario, boolean creando) {
		if(usuarioExiste(pUsuario)&&creando){
			return null;
		}
		//Cuando se actualice un auditor no es necesario volver a agregar el rol, por lo tanto solo si creando es verdadero se agreagará el rol,
		// sino simplemente se guardara el Auditor actualizado
		if(creando){
			//Agregar Rol al usuario auditor
			List<Role> roles =new ArrayList<>();
			pUsuario.setRoles(roles);
			Role role = roleRepository.findByNombre("ROLE_MINISTERIO");
			pUsuario.agregarRole(role);
			pUsuario.setContrasena(passwordEncoder.encode(pUsuario.getContrasena()));
			//Guardar auditor
			return save(pUsuario);
		}

		return save(pUsuario);

	}
	
	@Transactional("transactionManager")
	@Override
	public Usuario crearUsuario(String topic, Usuario pUsuario,String pRole, String clase, boolean creando) {
		if(usuarioExiste(pUsuario)&&creando) {
			return null;
		}
		List<Role> roles =new ArrayList<>(); 
		pUsuario.setRoles(roles);
		Role role = roleRepository.findByNombre(pRole);		
		pUsuario.agregarRole(role);
	
			UsuarioEvent event = new UsuarioEvent(
					pUsuario.getNumeroDocumento(),
					pUsuario.getNombre(),pUsuario.getCorreo(),
					pUsuario.getTipoDocumento().getId(),
					pUsuario.getTipoDocumento().getNombre(),
					pUsuario.getCelular());

			// Envía el evento de creación a Kafka de forma sincronico.

			ProducerRecord<String, Object> record = new ProducerRecord<>(topic,clase+"-"+pUsuario.getNumeroDocumento(),event);

			record.headers().add("messageId",Uuid.randomUuid().toString().getBytes());

			CompletableFuture<SendResult<String, Object>> future= kafkaTemplate.send(record);

			future.whenComplete((result, exception)->{
				if(exception!=null) {
						}
				else {
			
				}
			});
	
		if(!pRole.equals("ROLE_CLIENTE")&&creando) {
			pUsuario.setContrasena(passwordEncoder.encode(pUsuario.getContrasena()));
		}
		
		return save(pUsuario);
	}
	
	@Override
	public boolean usuarioExiste(Usuario pUsuario) {
		boolean existe=repository.buscarPreRegistro(pUsuario.getNumeroDocumento(), pUsuario.getCorreo(), pUsuario.getCelular())!=null;
		return existe;
	}

	@Override
	public Usuario buscarPorCorreo(String pCorreo) {
		Usuario usuario = repository.findByCorreo(pCorreo);
		if (usuario == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con el correo: " + pCorreo);
		}
		return usuario;
	}


	@Transactional("transactionManager")
	@Override
	public Usuario actualizarDatosUsuario(String pId, Usuario pUsuario) {
		Usuario buscado = repository.findById(pId).orElse(null);
		
		buscado.setCelular(pUsuario.getCelular());
		buscado.setNombre(pUsuario.getNombre());
		buscado.setCorreo(pUsuario.getCorreo());
		
		if(!buscado.getContrasena().equals(pUsuario.getContrasena())) {
			buscado.setContrasena(passwordEncoder.encode(pUsuario.getContrasena()));
		}
	
		buscado.setTipoDocumento(pUsuario.getTipoDocumento());
		
		return buscado;
	}

	@Transactional("transactionManager")
	@Override
	public Usuario updateUsuarioConRoles(String usuarioId, Usuario datosActualizados, List<String> nuevosRoles) {
		Usuario usuarioExistente = repository.findById(usuarioId)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		usuarioExistente.setNombre(datosActualizados.getNombre());
		usuarioExistente.setCelular(datosActualizados.getCelular());
		usuarioExistente.setCorreo(datosActualizados.getCorreo());
		usuarioExistente.setTipoDocumento(datosActualizados.getTipoDocumento());

		if(!usuarioExistente.getContrasena().equals(datosActualizados.getContrasena())) {
			usuarioExistente.setContrasena(passwordEncoder.encode(datosActualizados.getContrasena()));
		}
		// 2. Actualizar roles
		usuarioExistente.getRoles().clear();
		for (String nombreRol : nuevosRoles) {
			Role rol = roleRepository.findByNombre(nombreRol);
			usuarioExistente.getRoles().add(rol);
		}
		return usuarioExistente;
	}

	@Override
	public Usuario getCliente(String pId) {
		return repository.findById(pId).orElse(null);
	}

	@Override
	public boolean cambiarAccesoAlUsuario(String pNumeroDocumento) {
		Usuario usuario = repository.findById(pNumeroDocumento).orElse(null);
		usuario.setEnabled(!usuario.isEnabled());
		save(usuario);
		return usuario.isEnabled();
	}

	@Override
	public List<Usuario> findByRolesNombre(String pRoleNombre) {
		return repository.findByRolesNombre(pRoleNombre);
	}

	//Métodos para FEIGNCLIENT, conexion http a Auditores

	//Notifica la creacion de un Auditor en el microservicio Auditores
	@Transactional("transactionManager")
	@Override
	public ResponseEntity<?> notificarCreacionAuditores(String pNumeroDocumento, String pNombre) {
		return auditoresFeignClient.crearAuditor(pNumeroDocumento, pNombre);
	}

	//Actualiza un auditor que haya sido modificado en microservicio Auditores
	@Transactional("transactionManager")
	@Override
	public ResponseEntity<?> notificarActualizacionAuditores(String pNumeroDocumento, String pNombre) {
		return auditoresFeignClient.actualizarAuditor(pNumeroDocumento, pNombre);
	}
	
	@Override
    public Page<Usuario> findClientesPaginados(Long roleId, int pPagina) {
        Page<Usuario> usuarios = repository.findByRolesId(roleId, PageRequest.of(pPagina,25));
        return usuarios;
    }
	
	@Override
	public Usuario getDetailsOfUsuario(String pCorreo) {
		return repository.findByCorreoByRoles(pCorreo);
	}


	@Override
	public List<Usuario> validarDatos(String pNumeroDoc, String pCorreo, String pCelular) {
		return repository.validarCorreos(pNumeroDoc, pCorreo, pCelular);
	}

	@Override
	public String obtenerUsuarioDeToken(String pBearerToken) {
		try {
			String[] tokenParts = pBearerToken.split("\\.");
			String payload = new String(Base64.getDecoder().decode(tokenParts[1]));
			JSONObject payloadJson = new JSONObject(payload);
			String usuarioNumeroDocumento= payloadJson.getString("cc");
			return usuarioNumeroDocumento;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String obtenerRolDeToken(String pBearerToken) {
		try {
			String[] tokenParts = pBearerToken.split("\\.");

			String payload = new String(Base64.getDecoder().decode(tokenParts[1]));

			JSONObject payloadJson = new JSONObject(payload);

			List<Object> authorities = payloadJson.getJSONArray("authorities").toList();
			if (!authorities.isEmpty()) {
				return authorities.get(0).toString();
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	// Envio Kafka
	@Transactional("transactionManager")
	private void enviarEventoKafka(String topic, String clase, Usuario usuario) {

		UsuarioEvent event = new UsuarioEvent(
				usuario.getNumeroDocumento(),
				usuario.getNombre(),
				usuario.getCorreo(),
				usuario.getTipoDocumento().getId(),
				usuario.getTipoDocumento().getNombre(),
				usuario.getCelular()
		);

		ProducerRecord<String, Object> record = new ProducerRecord<>(
				topic,
				clase + "-" + usuario.getNumeroDocumento(),
				event
		);
		record.headers().add("messageId", Uuid.randomUuid().toString().getBytes());

		CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);
		future.whenComplete((result, exception) -> {
			if(exception != null) {
				// Manejar error si es necesario
			} else {
				// Manejar éxito si es necesario
			}
		});
	}

}
