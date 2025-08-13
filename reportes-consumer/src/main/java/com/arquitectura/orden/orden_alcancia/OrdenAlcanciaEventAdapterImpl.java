package com.arquitectura.orden.orden_alcancia;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.alcancia.entity.AlcanciaRepository;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.OrdenAlcanciaEvent;
import com.arquitectura.orden_alcancia.entity.OrdenAlcancia;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrdenAlcanciaEventAdapterImpl extends EventAdapterImpl<OrdenAlcancia, OrdenAlcanciaEvent> implements OrdenAlcanciaEventAdapter {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EventoRepository eventoRepository;
    
    @Autowired
    private AlcanciaRepository alcanciaRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Override
    public OrdenAlcancia creacion(OrdenAlcancia entity, OrdenAlcanciaEvent event)
    {
        entity.setId(event.getId());
        entity.setEstado(event.getEstado());
        entity.setTipo(event.getTipo());
        entity.setEvento(event.getEventoId() != null ? eventoRepository.findById(event.getEventoId()).orElse(null) : null);
        entity.setValorOrden(event.getValorOrden());
        entity.setCliente(event.getClienteId() != null ? clienteRepository.findById(event.getClienteId()).orElse(null) : null);
        entity.setValorSeguro(event.getValorSeguro());

        // Manejo seguro de tickets
        List<Ticket> tickets = new ArrayList<>();
        if (event.getTicketsIds() != null) {
            event.getTicketsIds().forEach(id -> {
                if (id != null) {
                    Ticket ticket = ticketRepository.findById(id).orElse(null);
                    if (ticket != null) {
                        tickets.add(ticket);
                    }
                }
            });
        }
        entity.setTickets(tickets);

        entity.setTarifa(event.getTarifaId() != null ? tarifaRepository.findById(event.getTarifaId()).orElse(null) : null);
        entity.setAlcancia(event.getAlcanciaId() != null ? alcanciaRepository.findById(event.getAlcanciaId()).orElse(null) : null);
        return super.creacion(entity,event);
    }

}
