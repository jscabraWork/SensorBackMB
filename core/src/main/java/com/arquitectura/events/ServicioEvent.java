package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServicioEvent implements BaseEvent{

    private Long id;

    private String nombre;

    private boolean utilizado;

    private Long ticketId;

}