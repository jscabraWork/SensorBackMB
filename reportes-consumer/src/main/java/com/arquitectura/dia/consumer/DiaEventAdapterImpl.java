package com.arquitectura.dia.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.DiaEvent;
import com.arquitectura.dia.entity.Dia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiaEventAdapterImpl extends EventAdapterImpl<Dia, DiaEvent> implements DiaEventAdapter {

    @Autowired
    private EventoRepository eventoRepository;

    @Override
    public Dia creacion(Dia entity, DiaEvent event) {
        entity.setId(event.getId());
        entity.setNombre(event.getNombre());
        entity.setFechaFin(event.getFechaFin());
        entity.setFechaInicio(event.getFechaInicio());
        entity.setEstado(event.getEstado());
        entity.setHoraInicio(event.getHoraInicio());
        entity.setHoraFin(event.getHoraFin());

        if (event.getEventoId() != null) {
            entity.setEvento(eventoRepository.findById(event.getEventoId()).orElse(null));
        }

        return super.creacion(entity, event);
    }
}
