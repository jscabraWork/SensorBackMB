package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventoEvent implements BaseEvent {

    private Long id;

    private String pulep;

    private String artistas;

    private String nombre;

    private LocalDateTime fechaApertura;

    private int estado;

    private Long tipoId;

    private Long venueId;

    private List<String> organizadoresId;

}
