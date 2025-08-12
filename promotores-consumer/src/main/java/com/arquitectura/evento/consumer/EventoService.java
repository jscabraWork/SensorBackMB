package com.arquitectura.evento.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.EventoEvent;

public interface EventoService extends CommonsConsumer<BaseEvent, EventoEvent, EntityDeleteEventLong> {
}
