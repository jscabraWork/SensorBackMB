package com.arquitectura.servicio.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.ServicioEvent;
import com.arquitectura.servicio.entity.Servicio;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServicioEventAdapterImpl extends EventAdapterImpl<Servicio, ServicioEvent> implements ServicioEventAdapter {

    @Autowired
    private TicketRepository ticketRepository;

    public Servicio creacion(Servicio entity, ServicioEvent event) {
        entity.setId(event.getId());
        entity.setNombre(event.getNombre());
        entity.setUtilizado(event.isUtilizado());
        entity.setTicket(ticketRepository.findById(event.getTicketId()).orElse(null));
        return super.creacion(entity, event);
    }

}
