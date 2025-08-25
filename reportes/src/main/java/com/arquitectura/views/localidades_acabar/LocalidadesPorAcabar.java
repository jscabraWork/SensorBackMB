package com.arquitectura.views.localidades_acabar;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "localidades_por_acabar")
public class LocalidadesPorAcabar {

    @Id
    @Column(name = "localidad_id")
    private Long localidadId;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "evento")
    private String evento;

    @Column(name = "total_tickets")
    private Long totalTickets;

    @Column(name = "tickets_vendidos")
    private Long ticketsVendidos;

    @Column(name = "tickets_disponibles")
    private Long ticketsDisponibles;

    @Column(name = "porcentaje_vendido")
    private Double porcentajeVendido;
}
