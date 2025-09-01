package com.arquitectura.intento_registro.entity;

import com.arquitectura.entity.Auditable;
import com.arquitectura.tipo_documento.entity.TipoDocumento;
import com.arquitectura.usuario.entity.Usuario;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Table(name = "intento_registro")
public class IntentoRegistro extends Auditable {

    @Id
    @GeneratedValue
    private Long id;

    private String numeroDocumento;

    private String nombre;

    private String correo;

    private String celular;

    private String contrasena;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_documento_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TipoDocumento tipoDocumento;

    private boolean activo;

    private boolean terminosYCondiciones;

    private boolean publicidad;

    private boolean tratamientoDeDatos;

    @Column(unique = true)
    private String idBusqueda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_numero_documento", referencedColumnName = "numeroDocumento", insertable = false, updatable = false)
    @JsonBackReference(value="usuario_intentos")
    private Usuario usuario;

    @Override
    @PrePersist
    public void prePersist() {
        super.prePersist();
        this.activo = true;
    }
}
