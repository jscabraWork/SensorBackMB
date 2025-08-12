package com.arquitectura.ticket.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.TicketPuntoFisicoEvent;
import com.arquitectura.localidad.entity.LocalidadRepository;
import com.arquitectura.puntofisico.entity.PuntoFisicoRepository;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TicketEventAdapterImpl extends EventAdapterImpl<Ticket, TicketPuntoFisicoEvent> implements TicketEventAdapter{

    @Autowired
    private PuntoFisicoRepository puntoFisicoRepositoryy;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public Ticket creacion(Ticket entity, TicketPuntoFisicoEvent event) {
        entity.setId(event.getId());
        entity.setEstado(event.getEstado());
        entity.setTipo(event.getTipo());
        entity.setNumero(event.getNumero());
        entity.setPuntofisico(puntoFisicoRepositoryy.findById(event.getPuntofisicoId()).orElse(null));
        entity.setLocalidad(localidadRepository.findById(event.getLocalidadId()).orElse(null));
        entity.setTarifa(tarifaRepository.findById(event.getTarifaId()).orElse(null));
        if(event.getPalcoId()!=null) {
            Ticket padre =ticketRepository.findById(event.getPalcoId()).orElse(null);
            if(padre != null) {
                entity.setPalco(padre);
            }
        }
        return super.creacion(entity, event);
    }
}
