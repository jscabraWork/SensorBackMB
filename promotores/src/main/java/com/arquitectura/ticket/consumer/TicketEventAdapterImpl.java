package com.arquitectura.ticket.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.TicketPromotorEvent;
import com.arquitectura.localidad.entity.LocalidadRepository;
import com.arquitectura.promotor.entity.PromotorRepository;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TicketEventAdapterImpl extends EventAdapterImpl<Ticket, TicketPromotorEvent> implements TicketEventAdapter{

    @Autowired
    private PromotorRepository promotorRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public Ticket creacion(Ticket entity, TicketPromotorEvent event) {
        entity.setId(event.getId());
        entity.setEstado(event.getEstado());
        entity.setTipo(event.getTipo());
        entity.setNumero(event.getNumero());

        if (event.getPromotorId() != null) {
            entity.setPromotor(promotorRepository.findByCorreoOrNumeroDocumento(event.getPromotorId()).orElse(null));
        }
        if (event.getLocalidadId() != null) {
            entity.setLocalidad(localidadRepository.findById(event.getLocalidadId()).orElse(null));
        }
        if (event.getTarifaId() != null) {
            entity.setTarifa(tarifaRepository.findById(event.getTarifaId()).orElse(null));
        }

        if(event.getPalcoId()!=null) {
            Ticket padre =ticketRepository.findById(event.getPalcoId()).orElse(null);
            if(padre != null) {
                entity.setPalco(padre);
            }
        }

        return super.creacion(entity, event);
    }
}
