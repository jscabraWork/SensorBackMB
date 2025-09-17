package com.arquitectura.usuario.controller;

import com.arquitectura.controller.CommonControllerString;
import com.arquitectura.rol.entity.Role;
import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UsuarioController extends CommonControllerString<Usuario, UsuarioService>{

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping({"/cliente/{pId}", "/organizador/{pId}", "/admin/{pId}", "/coordinador/{pId}", "/promotor/{pId}", "/punto/{pId}", "/contador/{pId}", "/auditor/{pId}"})
	public ResponseEntity<?> getUsuario(@PathVariable String pId){
		Map<String, Object> response = new HashMap<>();
		response.put("usuario", service.getCliente(pId));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/organizador")
	public ResponseEntity<?> crearOrganizador(@RequestBody Usuario pOrganizador) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearOrganizador(pOrganizador,true);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pOrganizador.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/admin")
	public ResponseEntity<?> crearAdministrador(@RequestBody Usuario pAdmin) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearAdministrador(pAdmin,true);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pAdmin.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/coordinador")
	public ResponseEntity<?> crearCoordinador(@RequestBody Usuario pCoordinador) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearCoordinador(pCoordinador,true);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pCoordinador.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/promotor")
	public ResponseEntity<?> crearPromotor(@RequestBody Usuario pPromotor) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearPromotor(pPromotor,true);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pPromotor.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/punto")
	public ResponseEntity<?> crearPuntoFisico(@RequestBody Usuario pPunto) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearPuntoFisico(pPunto,true);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pPunto.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/contador")
	public ResponseEntity<?> crearContador(@RequestBody Usuario pContador) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearContador(pContador,true);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pContador.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/auditor")
	@Transactional("transactionManager")
	public ResponseEntity<?> crearAuditor(@RequestBody Usuario pAuditor) {
		Map<String, Object> response = new HashMap<>();

		try {
			Usuario resultado = service.crearAuditor(pAuditor, true);

			if (resultado == null) {
				response.put("mensaje", "Los datos provisionados ya se encuentran registrados");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			// Intentar notificar la creación del auditor
			service.notificarCreacionAuditores(pAuditor.getNumeroDocumento(), pAuditor.getNombre());

			// Si llegamos aquí, tanto la creación como la notificación fueron exitosas
			response.put("mensaje", "Auditor con correo " + pAuditor.getCorreo() + " creado correctamente y notificado");
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			// Si ocurre cualquier error (en la creación o notificación), la transacción se revertirá
			response.put("mensaje", "Error al crear o notificar el auditor: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/crear/usuario")
	public ResponseEntity<?> crearUsuarioConRoles(@RequestBody Usuario usuario) {
		Map<String, Object> response = new HashMap<>();

		try {
			// Extraer la lista de roles del usuario
			List<String> roles = usuario.getRoles().stream()
					.map(Role::getNombre)
					.collect(Collectors.toList());

			Usuario resultado = service.crearUsuarioConMultiplesRoles(usuario, roles, true);

			if(resultado == null) {
				response.put("mensaje", "Los datos ingresados ya se encuentran registrados");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			response.put("mensaje", "Usuario creado exitosamente con los roles: " + String.join(", ", roles));
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.put("mensaje", "Error al crear el usuario: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/login/{username}")
	public ResponseEntity<?> loginTemporal(@PathVariable String username) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.buscarPorCorreo(username);

			response.put("usuario", resultado);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/actualizar/usuario")
	public ResponseEntity<?> updateUsuarioConRoles(@RequestBody Usuario pUsuario) {

		Map<String, Object> response = new HashMap<>();

		try {
			// Extraer nombres de roles del usuario recibido
			List<String> roles = pUsuario.getRoles().stream()
					.map(Role::getNombre)
					.collect(Collectors.toList());

			//actualizar datos del usuario
			//Este método no guarda, solo actualza los datos del usuario y sus roles
			Usuario usuarioActualizado = service.updateUsuarioConRoles(pUsuario.getNumeroDocumento(),pUsuario,roles);

			//Guardar y publicar en kafka
			Usuario usuario = service.crearUsuarioConMultiplesRoles(usuarioActualizado, roles, false);

			response.put("mensaje", "Usuario actualizado exitosamente");
			response.put("usuario", usuario);
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			response.put("mensaje", "Error al actualizar usuario: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
	@PutMapping("/cliente")
	public ResponseEntity<?> updateCliente(@RequestBody Usuario pCliente, @RequestHeader("Authorization")String pToken) {
		Map<String, Object> response = new HashMap<>();

		if(!pCliente.getNumeroDocumento().equals(service.obtenerUsuarioDeToken(pToken)) && !service.obtenerRolDeToken(pToken).equals("ROLE_ADMIN")){
			return new ResponseEntity<>("Ocurrió un Error ", HttpStatus.UNAUTHORIZED);
		}

		Usuario resultado=service.crearCliente(service.actualizarDatosUsuario(pCliente.getNumeroDocumento(), pCliente),false);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pCliente.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/organizador")
	public ResponseEntity<?> updateOrganizador(@RequestBody Usuario pOrganizador) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearOrganizador(service.actualizarDatosUsuario(pOrganizador.getNumeroDocumento(), pOrganizador),false);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pOrganizador.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/admin")
	public ResponseEntity<?> updateAdministrador(@RequestBody Usuario pAdmin) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearAdministrador(service.actualizarDatosUsuario(pAdmin.getNumeroDocumento(), pAdmin),false);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pAdmin.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/coordinador")
	public ResponseEntity<?> updateCoordinador(@RequestBody Usuario pCoordinador) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearCoordinador(service.actualizarDatosUsuario(pCoordinador.getNumeroDocumento(), pCoordinador),false);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pCoordinador.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/promotor")
	public ResponseEntity<?> updatePromotor(@RequestBody Usuario pPromotor) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearPromotor(service.actualizarDatosUsuario(pPromotor.getNumeroDocumento(), pPromotor),false);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pPromotor.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/punto")
	public ResponseEntity<?> updatePuntoFisico(@RequestBody Usuario pPunto) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearPuntoFisico(service.actualizarDatosUsuario(pPunto.getNumeroDocumento(), pPunto),false);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pPunto.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/contador")
	public ResponseEntity<?> updateContador(@RequestBody Usuario pContador) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearContador(service.actualizarDatosUsuario(pContador.getNumeroDocumento(), pContador),false);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pContador.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/auditor")
	public ResponseEntity<?> updateAuditor(@RequestBody Usuario pAuditor) {
		Map<String, Object> response = new HashMap<>();
		
		Usuario resultado=service.crearAuditor(service.actualizarDatosUsuario(pAuditor.getNumeroDocumento(), pAuditor),false);
		if(resultado==null) {
			response.put("mensaje","Los datos provisionados ya se encuentran registrados");
		}
		else {
			//Notificar a microservicio-auditores el Auditor actualizado
			service.notificarActualizacionAuditores(resultado.getNumeroDocumento(), resultado.getNombre());
			response.put("mensaje","Revisa tu correo, debió llegar un correo "+pAuditor.getCorreo()+"de confirmación para terminar el proceso de registro");
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	 @PutMapping("/enabled/{pNumeroDocumento}")
	 public ResponseEntity<?> cambiarEnabled(@PathVariable String pNumeroDocumento){
		 Map<String, Object> response = new HashMap<>();
		 response.put("enabled", service.cambiarAccesoAlUsuario(pNumeroDocumento));
		 return new ResponseEntity<>(response,HttpStatus.OK);
	 }
	 
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTADOR')")
		@GetMapping("/role/{pRoleName}")
		public ResponseEntity<?> getUsuariosPorRol(@PathVariable String pRoleName){
			Map<String, Object> response = new HashMap<>();
			response.put("usuarios", service.findByRolesNombre(pRoleName));
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		
	@PreAuthorize("hasRole('ADMIN')")
		@GetMapping("/role/{pRoleId}/{pPagina}")
		public ResponseEntity<?> getUsuariosPaginados(@PathVariable int pPagina,
													  @PathVariable Long pRoleId) {
			Map<String, Object> response = new HashMap<>();
			Page<Usuario> usuarios = service.findClientesPaginados(pRoleId, pPagina);
	        response.put("usuarios", usuarios);
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    }
		
		@GetMapping("/{pCorreo}/usuarioDetalles")
			public ResponseEntity<?> getAllDetailsOfUsuario(@PathVariable String pCorreo){
				Map<String, Object> response = new HashMap<>();
				Usuario detailsUsuario = service.getDetailsOfUsuario(pCorreo);
				response.put("correo", detailsUsuario);
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		
		@GetMapping("/correo/{pCorreo}")
		public Usuario getUsuarioPorCorreo(@PathVariable String pCorreo){
			return service.buscarPorCorreo(pCorreo);
		}

}
