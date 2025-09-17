package com.arquitectura.usuario.service;

import com.arquitectura.services.CommonServiceString;
import com.arquitectura.usuario.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UsuarioService extends CommonServiceString<Usuario>{

	public Usuario crearUsuarioConMultiplesRoles(Usuario pUsuario, List<String> roles, boolean creando);

	public Usuario crearCliente(Usuario pUsuario, boolean creando);
	
	public Usuario crearOrganizador(Usuario pUsuario, boolean creando);
	
	public Usuario crearPuntoFisico(Usuario pUsuario, boolean creando);
	
	public Usuario crearAdministrador(Usuario pUsuario, boolean creando);
	
	public Usuario crearPromotor(Usuario pUsuario, boolean creando);
	
	public Usuario crearAuditor(Usuario pUsuario, boolean creando);
	
	public Usuario crearCoordinador(Usuario pUsuario, boolean creando);
	
	public Usuario crearContador(Usuario pUsuario, boolean creando);
	
	public boolean usuarioExiste(Usuario pUsuario);
	
	public Usuario buscarPorCorreo(String pCorreo);
	
	public Usuario getCliente(String pId);
	
	public Usuario actualizarDatosUsuario(String pId, Usuario pUsuario);

	public Usuario updateUsuarioConRoles(String pId, Usuario pUsuario, List<String> nuevosRoles);
	
	public Usuario  crearUsuario(String topic, Usuario pUsuario,String role, String clase, boolean creando);
	
	public boolean cambiarAccesoAlUsuario(String pNumeroDocumento);

	public List<Usuario> findByRolesNombre(String pRoleNombre);

	Page<Usuario> findClientesPaginados(Long roleId, int cantidad);
	
	public Usuario getDetailsOfUsuario( String pCorreo);
	
	public List<Usuario> validarDatos(String pNumeroDoc, String pCorreo, String pCelular);

	public String obtenerUsuarioDeToken(String pBearerToken);

	public String obtenerRolDeToken(String pBearerToken);

}
