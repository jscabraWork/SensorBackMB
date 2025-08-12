package com.arquitectura.orden_promotor.entity;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.ticket.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "ordenes_promotor")
public class OrdenPromotor extends Orden {

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Promotor promotor;
}
