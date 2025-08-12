package com.arquitectura.evento.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.events.EventoEvent;
import com.arquitectura.venue.entity.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventoEventAdapterImpl extends EventAdapterImpl<Evento, EventoEvent> implements EventoEventAdapter {


    @Autowired
    private VenueRepository venueRepository;


    @Override
    public Evento creacion(Evento entity, EventoEvent event) {
        entity.setId(event.getId());
        entity.setPulep(event.getPulep());
        entity.setArtistas(event.getArtistas());
        entity.setNombre(event.getNombre());
        entity.setEstado(event.getEstado());
        entity.setFechaApertura(event.getFechaApertura());
        entity.setVenue(venueRepository.findById(event.getVenueId()).orElse(null));
        return super.creacion(entity, event);
    }
}
