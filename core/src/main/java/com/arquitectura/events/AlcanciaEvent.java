package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlcanciaEvent implements BaseEvent{

	private LocalDateTime creationDate;

	private LocalDateTime lastModifiedDate;

	private Long id;

	private Double precioParcialPagado;
	
	private Double precioTotal;
	
	private boolean activa;

	private String clienteNumeroDocumento;
	
	private List<Long> ticketsIds;
}
