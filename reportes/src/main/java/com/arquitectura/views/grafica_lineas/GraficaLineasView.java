package com.arquitectura.views.grafica_lineas;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GraficaLineasView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID compuesto implícito para JPA

    private Long eventoId;
    private LocalDate fecha;
    private Integer anio;
    private Integer mes;
    private Integer dia;
    private Long totalOrdenes;
    private Long totalTickets;
    private BigDecimal totalRecaudado;
    private BigDecimal totalPrecio;
    private BigDecimal totalServicio;
    private BigDecimal totalIva;

}
