package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransaccionEvent implements BaseEvent{

	private Long id;
	private Double amount;
    private String email;
    private String fullname;
    private String idPasarela;
    private String idPersona;
    private String ip;
    private int metodo;
    private String metodoNombre;
    private String phone;
    private int status;
    private String idBasePasarela;
  
	private Long ordenId;
	
}
