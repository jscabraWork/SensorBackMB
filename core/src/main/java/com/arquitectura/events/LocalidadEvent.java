package com.arquitectura.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalidadEvent implements BaseEvent {

    private Long id;

    private String nombre;

    //0: venta estandar, 1: localidad-alcancia
    private Integer tipo;

    private Double aporteMinimo;

    private String descripcion;

    private List<Long> diasIds;

}
