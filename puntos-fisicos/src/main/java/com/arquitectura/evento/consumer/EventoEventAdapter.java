package com.arquitectura.evento.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.events.EventoEvent;

public interface EventoEventAdapter extends EventAdapter<Evento, EventoEvent> {
}
