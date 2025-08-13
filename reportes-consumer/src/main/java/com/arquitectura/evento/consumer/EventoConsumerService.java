package com.arquitectura.evento.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EventoEvent;
import com.arquitectura.events.EntityDeleteEventLong;

public interface EventoConsumerService extends CommonsConsumer<BaseEvent, EventoEvent, EntityDeleteEventLong> {
}
