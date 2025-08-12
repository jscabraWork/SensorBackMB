package com.arquitectura.imagen.entity;

import com.arquitectura.evento.entity.Evento;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name="imagenes")
public class Imagen{

	@Id
	private Long id;
	
	private String nombre;
	
	private String url;
	
	//1 Perfil, 2 Banner, 3 QR, 4 Publicidad banner pagina Principal
	private int tipo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="evento_id")
	@JsonBackReference(value="eventoImagen_mov")
	private Evento evento;
}
