package com.arquitectura.evento.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.EventoEvent;
import com.arquitectura.evento.entity.Evento;

public interface EventoEventAdapter extends EventAdapter<Evento, EventoEvent> {
}
