package com.arquitectura.seguro.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.SeguroEvent;
import com.arquitectura.seguro.entity.Seguro;
import org.springframework.stereotype.Component;

@Component
public class SeguroEventAdapterImpl extends EventAdapterImpl<Seguro, SeguroEvent> implements SeguroEventAdapter {

    @Override
    public Seguro creacion(Seguro entity, SeguroEvent event) {
        entity.setId(event.getId());
        entity.setValor(event.getValor());
        entity.setReclamado(event.isReclamado());

        return super.creacion(entity, event);
    }
}