package com.arquitectura.usuario_auth_provider.entity;

import com.arquitectura.entity.Auditable;
import com.arquitectura.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "usuarios_auth_providers")
public class UsuarioAuthProvider extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_numero_documento", referencedColumnName = "numeroDocumento", nullable = false)
    private Usuario usuario;

    // 0 = Google
    @Column(nullable = false)
    private int tipo;

    @Column(name = "provider_user_id", unique = true, nullable = false, length = 100)
    private String providerUserId;

    @Column(name = "access_token", length = 500)
    private String accessToken;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @PrePersist
    public void prePersist() {
        LocalDateTime date = LocalDateTime.now();
        setCreationDate(date);
    }

    // Constructor específico para crear con parámetros básicos
    public UsuarioAuthProvider(Usuario usuario, int tipo, String providerUserId) {
        this.usuario = usuario;
        this.tipo = tipo;
        this.providerUserId = providerUserId;
        this.emailVerified = true;
    }

    // Métodos utilitarios para obtener el nombre del proveedor
    public String getProviderName() {
        switch (tipo) {
            case 0: return "GOOGLE";
            case 1: return "FACEBOOK";
            case 2: return "APPLE";
            default: return "UNKNOWN";
        }
    }
}