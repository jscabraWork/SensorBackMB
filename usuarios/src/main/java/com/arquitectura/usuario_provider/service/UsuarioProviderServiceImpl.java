package com.arquitectura.usuario_provider.service;

import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.usuario.entity.UsuarioRepository;
import com.arquitectura.usuario.service.UsuarioService;
import com.arquitectura.usuario_auth_provider.entity.UsuarioAuthProvider;
import com.arquitectura.usuario_auth_provider.entity.UsuarioAuthProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UsuarioProviderServiceImpl implements UsuarioProviderService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioAuthProviderRepository usuarioAuthProviderRepository;

    @Value("${clientes.topic}")
    private String clientesTopic;

    @Override
    @Transactional("transactionManager")
    public Usuario crearClienteConProvider(Usuario usuario, int tipoProvider, String providerId, String accessToken, String refreshToken) {

        if(usuarioService.usuarioExiste(usuario)) {
            throw new IllegalStateException("Los datos provisionados ya se encuentran registrados");
        }

        if(existeUsuarioConMismosDatosPeroDiferenteCorreo(usuario)) {
            throw new IllegalStateException("Los datos ya están registrados con un correo diferente. " +
                    "Comunicate con servicio al cliente para actualizar tu correo.");
        }

        usuario.setContrasena("Google");
        Usuario usuarioCreado = usuarioService.crearCliente(usuario, true);

        UsuarioAuthProvider authProvider = new UsuarioAuthProvider(
                usuarioCreado,
                tipoProvider,
                providerId
        );
        authProvider.setAccessToken(accessToken);
        usuarioAuthProviderRepository.save(authProvider);
        return usuarioCreado;
    }

    @Override
    public boolean existeUsuarioConMismosDatosPeroDiferenteCorreo(Usuario usuario) {
        return usuarioRepository.findByIdOCelular(usuario.getNumeroDocumento(), usuario.getCelular(), usuario.getCorreo());
    }

    @Override
    @Transactional("transactionManager")
    public Usuario asociarProviderAUsuarioExistente(String correo, int tipoProvider, String providerId, String accessToken) {

        Usuario usuarioExistente = usuarioRepository.findByCorreo(correo);
        if (usuarioExistente == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        if(existeUsuarioConMismosDatosPeroDiferenteCorreo(usuarioExistente)) {
            throw new IllegalStateException("Los datos (documento/celular) ya están registrados con un correo diferente. " +
                    "No se puede asociar el proveedor.");
        }

        Optional<UsuarioAuthProvider> existente = usuarioAuthProviderRepository
                .findByUsuario_NumeroDocumentoAndTipo(usuarioExistente.getNumeroDocumento(), tipoProvider);

        UsuarioAuthProvider authProvider;
        if (existente.isPresent()) {
            authProvider = existente.get();
        } else {
            authProvider = new UsuarioAuthProvider(
                    usuarioExistente,
                    tipoProvider,
                    providerId
            );
        }

        authProvider.setProviderUserId(providerId);
        authProvider.setAccessToken(accessToken);

        usuarioAuthProviderRepository.save(authProvider);
        return usuarioExistente;
    }

    @Override
    public Usuario getUsuarioByProviderId(String providerId, int tipoProvider) {
        return usuarioAuthProviderRepository.findByProviderUserId(providerId)
                .map(authProvider -> {
                    Usuario usuario = authProvider.getUsuario();

                    if (usuario != null) {
                        usuario.getNumeroDocumento();
                        usuario.getNombre();
                        usuario.getCorreo();
                        usuario.getCelular();
                        usuario.getTipoDocumento();
                        usuario.isEnabled();
                        usuario.getContrasena();

                        if (usuario.getRoles() != null) {
                            usuario.getRoles().size();
                        }

                        Usuario usuarioSimple = new Usuario();
                        usuarioSimple.setNumeroDocumento(usuario.getNumeroDocumento());
                        usuarioSimple.setNombre(usuario.getNombre());
                        usuarioSimple.setCorreo(usuario.getCorreo());
                        usuarioSimple.setCelular(usuario.getCelular());
                        usuarioSimple.setTipoDocumento(usuario.getTipoDocumento());
                        usuarioSimple.setEnabled(usuario.isEnabled());
                        usuarioSimple.setContrasena(usuario.getContrasena());
                        usuarioSimple.setRoles(usuario.getRoles());

                        return usuarioSimple;
                    }
                    return null;
                })
                .orElse(null);
    }
}
