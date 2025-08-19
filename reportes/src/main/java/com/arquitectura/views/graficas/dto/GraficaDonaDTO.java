package com.arquitectura.views.graficas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraficaDonaDTO {

    private String metodo;
    private Double totalRecaudado;
    private Double totalRecaudadoTransacciones;

}
