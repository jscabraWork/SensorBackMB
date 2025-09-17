package com.arquitectura.usuario_auth_provider.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioAuthProviderRepository extends JpaRepository<UsuarioAuthProvider, Long> {

    // Buscar por provider user id (googleId, appleId, facebookId)
    Optional<UsuarioAuthProvider> findByProviderUserId(String providerUserId);

    // Buscar por usuario y tipo de proveedor
    Optional<UsuarioAuthProvider> findByUsuario_NumeroDocumentoAndTipo(String numeroDocumento, int tipo);

    // Buscar todos los proveedores de un usuario
    List<UsuarioAuthProvider> findByUsuario_NumeroDocumento(String numeroDocumento);

    // Buscar por tipo de proveedor
    List<UsuarioAuthProvider> findByTipo(int tipo);

    // Verificar si un usuario tiene un proveedor específico
    boolean existsByUsuario_NumeroDocumentoAndTipo(String numeroDocumento, int tipo);

    // Eliminar por usuario y tipo
    void deleteByUsuario_NumeroDocumentoAndTipo(String numeroDocumento, int tipo);
}