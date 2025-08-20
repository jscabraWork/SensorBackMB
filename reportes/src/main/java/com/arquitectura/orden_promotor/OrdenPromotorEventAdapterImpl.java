package com.arquitectura.orden_promotor;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.OrdenPromotorEvent;
import com.arquitectura.orden_promotor.entity.OrdenPromotor;
import com.arquitectura.promotor.entity.PromotorRepository;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrdenPromotorEventAdapterImpl extends EventAdapterImpl<OrdenPromotor, OrdenPromotorEvent> implements OrdenPromotorEventAdapter {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private PromotorRepository promotorRepository;

    @Override
    public OrdenPromotor creacion(OrdenPromotor entity, OrdenPromotorEvent event)
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
        if (event.getPromotorId() != null) {
            entity.setPromotor(promotorRepository.findByCorreoOrNumeroDocumento(event.getPromotorId()).orElse(null));
        }
        return super.creacion(entity,event);
    }

}
