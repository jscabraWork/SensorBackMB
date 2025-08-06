package com.arquitectura.recuperacion.entity;

import java.time.LocalDateTime;

import com.arquitectura.usuario.entity.Usuario;
import com.arquitectura.entity.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="recuperacion")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Recuperacion extends Auditable {

	@Id
	@GeneratedValue
	private Long id;
	
	private boolean activo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="usuario_id")
	@JsonIgnore
	private Usuario recuperacionUsuario;
	
	@Column(unique=true)
	private String idBusqueda;
	
	@PrePersist
	public void prePersist() {
		activo=true;
		LocalDateTime date = LocalDateTime.now();
		setCreationDate(date);
	}
	
}
