package com.arquitectura.codigo_validacion.entity;

import java.time.LocalDateTime;

import com.arquitectura.entity.Auditable;
import com.arquitectura.usuario.entity.Usuario;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name="codigos")
public class Codigo extends Auditable{

	@Id
	@GeneratedValue
	private Long id;
	
	private String numeroDocumento;
	
	private String nombre;
	
	private String contrasena;
	
	private String correo;
	
	private String celular;
	
	private String tipo_documento;
	
	private String codigo;
	
	private boolean activo;
	
	private boolean publicidad;
	
	@Column(unique = true)
	private String idBusqueda;
	
	@OneToOne
	@JsonBackReference(value="usuario_mov")
	private Usuario usuario;
	
	@PrePersist
	public void prePersist() {
		LocalDateTime date = LocalDateTime.now();
		setCreationDate(date);
		LocalDateTime date2 = date.plusMinutes(30);
		setLastModifiedDate(date2);
		this.activo=true;
	}
}
