package com.arquitectura.usuario.entity;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.arquitectura.codigo_validacion.entity.Codigo;
import com.arquitectura.entity.Auditable;
import com.arquitectura.intento_registro.entity.IntentoRegistro;
import com.arquitectura.rol.entity.Role;
import com.arquitectura.tipo_documento.entity.TipoDocumento;
import com.fasterxml.jackson.annotation.JsonBackReference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name="usuarios")
public class Usuario extends Auditable implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6282009350044979537L;

	@Id
	private String numeroDocumento;
	
	private String nombre;
	
	@Column(length=60)
	private String contrasena;
	
	@Column(unique=true,length=100)
	private String correo;
	
	@Column(unique=true, length=30)
	private String celular;

	private boolean enabled;

	@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference(value="usuario_intentos")
	private List<IntentoRegistro> intentosRegistro;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tipo_documento_id")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private TipoDocumento tipoDocumento;

	@ManyToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(
			name = "usuarios_roles",
			joinColumns = @JoinColumn(name = "usuario_numero_documento"),
			inverseJoinColumns = @JoinColumn(name = "roles_id"),
			uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_numero_documento", "roles_id"})
	)
	private List<Role> roles = new ArrayList<>();;
	
	@PrePersist
	public void prePersist() {
		enabled=true;
		LocalDateTime date = LocalDateTime.now();
		setCreationDate(date);
	}
	public void agregarRole(Role rol) {
		roles.add(rol);
	}
}
