package com.arquitectura.views.graficas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraficaLineasDTO {

    private Integer periodo; //Puede ser meses o dias

    private String periodoNombre; // Nombre del periodo (ej. "Enero", "Miercoles")

    private BigDecimal recaudado;

    private Long asistentes;

    private BigDecimal precioRecaudado;

    private BigDecimal servicioRecaudado;

    private BigDecimal ivaRecaudado;

    private BigDecimal recaudadoTrx;

}
