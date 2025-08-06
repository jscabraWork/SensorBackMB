package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SeguroEvent implements BaseEvent{

    private Long id;

    private Double valor;

    private boolean reclamado;

}
