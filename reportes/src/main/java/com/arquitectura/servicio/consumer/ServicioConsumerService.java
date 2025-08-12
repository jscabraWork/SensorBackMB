package com.arquitectura.servicio.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.ServicioEvent;

public interface ServicioConsumerService extends CommonsConsumer<BaseEvent, ServicioEvent, EntityDeleteEventLong> {
}
