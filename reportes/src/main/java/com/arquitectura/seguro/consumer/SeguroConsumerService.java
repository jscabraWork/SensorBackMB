package com.arquitectura.seguro.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.SeguroEvent;

public interface SeguroConsumerService extends CommonsConsumer<BaseEvent, SeguroEvent, EntityDeleteEventLong> {
}
