package com.arquitectura.orden_puntofisico.entity;


import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.ticket.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name="ordenes_puntosfisicos")
public class OrdenPuntoFisico extends Orden {

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private PuntoFisico puntoFisico;

}
