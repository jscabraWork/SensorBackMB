package com.arquitectura.orden_traspaso.entity;

import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.ticket.entity.Ticket;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="ordenes_traspaso")
public class OrdenTraspaso extends Orden {

    //Cliente que recibe el traspaso
    @ManyToOne
    @JoinColumn(name = "cliente_receptor_id")
    private Cliente receptor;

    public OrdenTraspaso(Cliente receptor, Cliente cliente, List<Ticket> tickets) {
        this.receptor = receptor;
        this.cliente = cliente;
        tipo = 5;
        estado = 1;
        Localidad localidad = tickets.get(0).getLocalidad();
        tarifa = tickets.get(0).getTarifa();
        evento = localidad.getEvento();
        valorSeguro =0.0;
        //El método setTickets tiene en cuenta asientos de tickets que se agregan individualmente a la orden
        super.setTickets(tickets);
        this.valorOrden = super.calcularValorOrden();
    }

}
