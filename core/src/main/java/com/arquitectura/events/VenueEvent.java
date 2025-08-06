package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueEvent implements BaseEvent {

    private Long id;

    private String nombre;

    private String urlMapa;

    private Long mapaId;

    private int aforo;

    private Long ciudadId;
}
