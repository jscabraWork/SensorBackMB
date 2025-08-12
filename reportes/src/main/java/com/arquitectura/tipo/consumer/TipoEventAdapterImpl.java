package com.arquitectura.tipo.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.TipoEvent;
import com.arquitectura.tipo.entity.Tipo;
import org.springframework.stereotype.Component;

@Component
public class TipoEventAdapterImpl extends EventAdapterImpl<Tipo, TipoEvent> implements TipoEventAdapter {

    @Override
    public Tipo creacion(Tipo entity, TipoEvent event) {
        entity.setId(event.getId());
        entity.setNombre(event.getNombre());
        return super.creacion(entity, event);
    }
}
