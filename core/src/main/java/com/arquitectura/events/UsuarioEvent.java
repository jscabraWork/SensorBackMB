package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioEvent implements BaseEvent{

	private String id;
	
	private String nombre;
	
	private String correo;

	private Long tipoDocumentoId;

	private String tipoDocumento;

	private String celular;
}
