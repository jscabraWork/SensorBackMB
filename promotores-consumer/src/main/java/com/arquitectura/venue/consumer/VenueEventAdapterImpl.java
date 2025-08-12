package com.arquitectura.venue.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.ciudad.entity.CiudadRepository;
import com.arquitectura.events.VenueEvent;
import com.arquitectura.venue.entity.Venue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VenueEventAdapterImpl extends EventAdapterImpl<Venue, VenueEvent> implements VenueEventAdapter {

    @Autowired
    private CiudadRepository ciudadRepository;

    @Override
    public Venue creacion(Venue entity, VenueEvent event) {

        entity.setId(event.getId());
        entity.setNombre(event.getNombre());
        entity.setUrlMapa(event.getUrlMapa());

        if(event.getCiudadId() != null) {
            Ciudad ciudad = ciudadRepository.findById(event.getCiudadId()).orElse(null);
            entity.setCiudad(ciudad);
        }

        return super.creacion(entity, event);
    }
}
