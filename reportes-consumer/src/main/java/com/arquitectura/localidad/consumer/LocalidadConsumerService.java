package com.arquitectura.localidad.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.LocalidadEvent;
import com.arquitectura.events.EntityDeleteEventLong;

public interface LocalidadConsumerService extends CommonsConsumer<BaseEvent, LocalidadEvent, EntityDeleteEventLong> {
}
