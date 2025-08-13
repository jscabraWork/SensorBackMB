package com.arquitectura.orden.orden_puntofisico;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.OrdenEvent;
import com.arquitectura.events.OrdenPuntoFisicoEvent;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisicoRepository;
import com.arquitectura.puntofisico.entity.PuntoFisicoRepository;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrdenPuntoFisicoEventAdapterImpl extends EventAdapterImpl<OrdenPuntoFisico, OrdenPuntoFisicoEvent> implements OrdenPuntoFisicoEventAdapter {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private PuntoFisicoRepository puntoFisicoRepository;

    @Override
    public OrdenPuntoFisico creacion(OrdenPuntoFisico entity, OrdenPuntoFisicoEvent event)
    {
        entity.setId(event.getId());
        entity.setEstado(event.getEstado());
        entity.setTipo(event.getTipo());
        entity.setEvento(eventoRepository.findById(event.getEventoId()).orElse(null));
        entity.setValorOrden(event.getValorOrden());
        entity.setCliente(clienteRepository.findById(event.getClienteId()).orElse(null));
        entity.setValorSeguro(event.getValorSeguro());

        // Manejo seguro de tickets
        List<Ticket> tickets = new ArrayList<>();
        if (event.getTicketsIds() != null) {
            event.getTicketsIds().forEach(id -> {
                Ticket ticket = ticketRepository.findById(id).orElse(null);
                tickets.add(ticket);
            });
        }
        entity.setTickets(tickets);
        entity.setTarifa(tarifaRepository.findById(event.getTarifaId()).orElse(null));
        entity.setPuntoFisico(puntoFisicoRepository.findById(event.getPuntoFisicoId()).orElse(null));
        return super.creacion(entity,event);
    }

}
