package com.arquitectura.orden_traspaso;
import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.OrdenTraspasoEvent;
import com.arquitectura.orden_traspaso.entity.OrdenTraspaso;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrdenTraspasoEventAdapterImpl extends EventAdapterImpl<OrdenTraspaso, OrdenTraspasoEvent> implements OrdenTraspasoEventAdapter {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Override
    public OrdenTraspaso creacion(OrdenTraspaso entity, OrdenTraspasoEvent event)
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
        entity.setReceptor(clienteRepository.findById(event.getClienteReceptorId()).orElse(null));
        return super.creacion(entity,event);
    }

}
