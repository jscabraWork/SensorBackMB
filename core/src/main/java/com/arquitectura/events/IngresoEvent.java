package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IngresoEvent implements BaseEvent{

    private Long id;

    private boolean utilizado;

    private Long ticketId;

    private Long diaId;

    private LocalDateTime fechaIngreso;


}
