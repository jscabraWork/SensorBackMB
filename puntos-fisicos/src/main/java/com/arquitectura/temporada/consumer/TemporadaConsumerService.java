package com.arquitectura.temporada.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TemporadaEvent;

public interface TemporadaConsumerService extends CommonsConsumer<BaseEvent, TemporadaEvent, EntityDeleteEventLong> {
}
