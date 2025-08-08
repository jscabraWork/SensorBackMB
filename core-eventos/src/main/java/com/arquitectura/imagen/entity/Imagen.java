package com.arquitectura.imagen.entity;

import com.arquitectura.entity.Auditable;
import com.arquitectura.evento.entity.Evento;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name="imagenes")
public class Imagen extends Auditable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Long id;
	
	private String nombre;
	
	private String url;
	
	//1 Perfil, 2 Banner, 3 QR, 4 Publicidad banner pagina Principal
	private int tipo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="evento_id")
	@JsonBackReference(value="eventoImagen_mov")
	private Evento evento;

	public Imagen (String nombre, int tipo, Evento evento) {
		this.nombre = setNombreImagen(tipo,evento.getNombre());
		this.url = "https://marcablanca.allticketscol.com/"+nombre;
		this.tipo = tipo;
		this.evento = evento;
	}

	public String setNombreImagen(int tipo, String pNombreEvento){
		String nombreImagen = "";
		switch (tipo) {
			case 1:
				nombreImagen = "principal_" + pNombreEvento;
				break;
			case 2:
				nombreImagen = "banner_" + pNombreEvento;
				break;
			case 3:
				nombreImagen = "qr_" + pNombreEvento;
				break;
			case 4:
				nombreImagen = "publicidad_banner_" + pNombreEvento;
				break;
			case 5:
				nombreImagen = "mapa_" + pNombreEvento;
				break;
			default:
				nombreImagen = "imagen_" + pNombreEvento;
		}
		return nombreImagen;
	}

}
