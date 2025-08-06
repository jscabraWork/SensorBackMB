package com.arquitectura.service;

import com.arquitectura.usuario.entity.Usuario;

public interface IUsuarioService {

	public Usuario findByCorreo(String pCorreo);
}
