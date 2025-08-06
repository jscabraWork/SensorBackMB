package com.arquitectura.intento_registro.entity;

import com.arquitectura.usuario.entity.Usuario;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "intento_registro")
public class IntentoRegistro {

    @Id
    @GeneratedValue
    private Long id;

    private String numeroDocumento;

    private String nombre;

    private String correo;

    private String celular;

    private String contraseña;

    private String tipoDocumento;

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
}
