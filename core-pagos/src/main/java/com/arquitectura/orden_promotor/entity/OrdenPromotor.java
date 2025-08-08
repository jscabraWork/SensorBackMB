package com.arquitectura.orden_promotor.entity;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "ordenes_promotor")
@AllArgsConstructor
public class OrdenPromotor extends Orden {

    @Column(name = "promotorId")
    private String promotorNumeroDocumento;

    /**
     * Constructor para crear una orden de promotor.
     *
     * @param evento                Evento asociado a la orden.
     * @param cliente               Cliente que realiza la orden.
     * @param tickets               Lista de tickets asociados a la orden.
     * @param promotorNumeroDocumento Número de documento del promotor.
     */
    public OrdenPromotor(Evento evento, Cliente cliente, List<Ticket> tickets, String promotorNumeroDocumento) {
        super(evento, cliente, tickets, null);
        this.promotorNumeroDocumento = promotorNumeroDocumento;
    }
}
