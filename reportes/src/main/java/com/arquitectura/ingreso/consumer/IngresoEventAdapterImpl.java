package com.arquitectura.ingreso.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.dia.entity.DiaRepository;
import com.arquitectura.events.IngresoEvent;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IngresoEventAdapterImpl extends EventAdapterImpl<Ingreso, IngresoEvent> implements IngresoEventAdapter {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private DiaRepository diaRepository;

    @Override
    public Ingreso creacion(Ingreso entity, IngresoEvent event) {
        entity.setId(event.getId());
        entity.setUtilizado(event.isUtilizado());
        entity.setFechaIngreso(event.getFechaIngreso());

        // Manejo seguro de relaciones
        if (event.getTicketId() != null) {
            entity.setTicket(ticketRepository.findById(event.getTicketId()).orElse(null));
        }

        if (event.getDiaId() != null) {
            entity.setDia(diaRepository.findById(event.getDiaId()).orElse(null));
        }
        return super.creacion(entity, event);
    }
}