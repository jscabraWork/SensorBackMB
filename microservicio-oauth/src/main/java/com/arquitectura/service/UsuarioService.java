package com.arquitectura.service;

import com.arquitectura.usuario.entity.Usuario;

public interface UsuarioService {

	public Usuario findByCorreo(String pCorreo);

    public Usuario getUsuarioByProviderId(String providerId, int tipoProvider);
}
