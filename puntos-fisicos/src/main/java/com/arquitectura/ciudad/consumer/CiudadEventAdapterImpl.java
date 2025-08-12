package com.arquitectura.ciudad.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.events.CiudadEvent;
import org.springframework.stereotype.Component;

@Component
public class CiudadEventAdapterImpl extends EventAdapterImpl<Ciudad, CiudadEvent> implements CiudadEventAdapter {

    @Override
    public Ciudad creacion(Ciudad entity, CiudadEvent event) {

        entity.setId(event.getId());
        entity.setNombre(event.getNombre());
        return super.creacion(entity, event);
    }
}
