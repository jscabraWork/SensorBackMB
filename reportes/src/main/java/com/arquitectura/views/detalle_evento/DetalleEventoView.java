package com.arquitectura.views.detalle_evento;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Entity para la vista detalle_ventas
 * Representa el detalle de ventas por evento con métricas completas
 */
@Entity
@Table(name = "detalle_ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Immutable
public class DetalleEventoView {

    @Id
    @Column(name = "tarifa_id")
    private Long tarifaId;

    @Column(name = "evento_id")
    private Long eventoId;

    private String tarifa;

    private String localidad;

    private String dia;

    @Column(name = "localidad_id")
    private Long localidadId;

    @Column(name = "dia_id")
    private Long diaId;

    private Double precio;

    private Double servicio;

    private Double iva;

    @Column(name = "precio_total")
    private Double precioTotal;

    private Long vendidos;

    private Long reservados;

    private Long proceso;

    private Long disponibles;

    @Column(name = "total_tickets")
    private Long totalTickets;

    @Column(name = "total_precio")
    private Double totalPrecio;

    @Column(name = "total_servicio")
    private Double totalServicio;

    @Column(name = "total_iva")
    private Double totalIva;

    @Column(name = "total_recaudado")
    private Double totalRecaudado;

    private Long utilizados;


    // Métodos de cálculo útiles
    @JsonIgnore
    public Long getTotalVendidosYReservados() {
        return (vendidos != null ? vendidos : 0) + (reservados != null ? reservados : 0);
    }

    public Double getPorcentajeOcupacion() {
        if (totalTickets == null || totalTickets == 0) return 0.0;
        return (double) getTotalVendidosYReservados() / totalTickets * 100;
    }

}