package com.arquitectura.orden.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenEvent;

public interface OrdenConsumerService extends CommonsConsumer<BaseEvent, OrdenEvent, EntityDeleteEventLong> {
}
