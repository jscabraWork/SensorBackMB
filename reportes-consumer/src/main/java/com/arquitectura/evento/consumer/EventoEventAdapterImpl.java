package com.arquitectura.evento.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.EventoEvent;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.organizador.entity.OrganizadorRepository;
import com.arquitectura.tipo.entity.TipoRepository;
import com.arquitectura.venue.entity.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventoEventAdapterImpl extends EventAdapterImpl<Evento, EventoEvent> implements EventoEventAdapter {

    @Autowired
    private TipoRepository tipoRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private OrganizadorRepository organizadorRepository;

    @Override
    public Evento creacion(Evento entity, EventoEvent event) {
        entity.setId(event.getId());
        entity.setPulep(event.getPulep());
        entity.setArtistas(event.getArtistas());
        entity.setNombre(event.getNombre());
        entity.setEstado(event.getEstado());
        entity.setFechaApertura(event.getFechaApertura());
        entity.setTipo(tipoRepository.findById(event.getTipoId()).orElse(null));
        entity.setVenue(venueRepository.findById(event.getVenueId()).orElse(null));
        entity.setOrganizadores(organizadorRepository.findAllById(event.getOrganizadoresId()));

        return super.creacion(entity, event);
    }
}
