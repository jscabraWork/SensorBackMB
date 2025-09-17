package com.arquitectura.usuario_provider.service;

import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario_auth_provider.entity.UsuarioAuthProvider;

public interface UsuarioProviderService {

    Usuario crearClienteConProvider(Usuario usuario, int tipoProvider, String providerId, String accessToken, String refreshToken);

    Usuario asociarProviderAUsuarioExistente(String correo, int tipoProvider, String providerId, String accessToken);

    boolean existeUsuarioConMismosDatosPeroDiferenteCorreo(Usuario usuario);

    Usuario getUsuarioByProviderId(String providerId, int tipoProvider);
}
