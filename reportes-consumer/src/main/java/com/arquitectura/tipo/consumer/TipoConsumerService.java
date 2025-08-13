package com.arquitectura.tipo.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.TipoEvent;
import com.arquitectura.events.EntityDeleteEventLong;

public interface TipoConsumerService extends CommonsConsumer<BaseEvent, TipoEvent, EntityDeleteEventLong> {
}
