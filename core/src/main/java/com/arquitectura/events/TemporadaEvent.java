package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemporadaEvent implements BaseEvent {

    private Long id;

    private String nombre;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private Integer estado;
}
