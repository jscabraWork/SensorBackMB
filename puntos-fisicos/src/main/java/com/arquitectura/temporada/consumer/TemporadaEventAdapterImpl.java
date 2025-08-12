package com.arquitectura.temporada.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.TemporadaEvent;
import com.arquitectura.temporada.entity.Temporada;
import org.springframework.stereotype.Component;

@Component
public class TemporadaEventAdapterImpl extends EventAdapterImpl<Temporada, TemporadaEvent> implements TemporadaEventAdapter {


    @Override
    public Temporada creacion(Temporada entity, TemporadaEvent event) {
        entity.setId(event.getId());
        entity.setNombre(event.getNombre());
        entity.setFechaInicio(event.getFechaInicio());
        entity.setFechaFin(event.getFechaFin());
        entity.setEstado(event.getEstado());
        return super.creacion(entity, event);
    }
}
