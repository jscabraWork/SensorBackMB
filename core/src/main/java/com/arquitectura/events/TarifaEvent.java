package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TarifaEvent implements BaseEvent {

    private Long id;

    private String nombre;

    private Double precio;

    private Double servicio;

    private Double iva;

    private Integer estado;

    private Long localidadId;


}
