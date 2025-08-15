package com.arquitectura.seguro.entity;

import com.arquitectura.configSeguro.entity.ConfigSeguro;
import com.arquitectura.ticket.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "seguros")
public class Seguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valor;

    private boolean reclamado;

    @ManyToOne
    @JoinColumn(name = "configSeguro_id")
    @JsonIgnore
    private ConfigSeguro configSeguro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    public Seguro(Double valor, ConfigSeguro configSeguro, Ticket ticket) {
        this.valor = valor;
        this.configSeguro = configSeguro;
        this.ticket = ticket;
        this.reclamado = false; // Por defecto, el seguro no está reclamado al crearlo
    }

}
