package com.arquitectura.orden_puntofisico.entity;


import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.ticket.entity.Ticket;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name="ordenes_puntosfisicos")
@AllArgsConstructor
@NoArgsConstructor
public class OrdenPuntoFisico extends Orden {

    @Column(name = "puntoId")
    private String puntoFisicoNumeroDocumento;

    /**
     * Constructor para crear una orden de promotor.
     *
     * @param evento                Evento asociado a la orden.
     * @param cliente               Cliente que realiza la orden.
     * @param tickets               Lista de tickets asociados a la orden.
     * @param puntoFisicoNumeroDocumento Número de documento del puntofisico.
     */
    public OrdenPuntoFisico(Evento evento, Cliente cliente, List<Ticket> tickets, String puntoFisicoNumeroDocumento) {
        super(evento, cliente, tickets, null);
        this.puntoFisicoNumeroDocumento = puntoFisicoNumeroDocumento;
    }

    /**
     * Constructor para crear una orden de punto físico con tarifa específica.
     *
     * @param evento                Evento asociado a la orden.
     * @param cliente               Cliente que realiza la orden.
     * @param tickets               Lista de tickets asociados a la orden.
     * @param tarifa                Tarifa específica para la orden.
     * @param puntoFisicoNumeroDocumento Número de documento del puntofisico.
     */
    public OrdenPuntoFisico(Evento evento, Cliente cliente, List<Ticket> tickets, Tarifa tarifa, String puntoFisicoNumeroDocumento) {
        super(evento, cliente, tickets, tarifa, null);
        this.puntoFisicoNumeroDocumento = puntoFisicoNumeroDocumento;
    }

}
