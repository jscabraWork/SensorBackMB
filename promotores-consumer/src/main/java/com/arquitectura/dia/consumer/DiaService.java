package com.arquitectura.dia.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.DiaEvent;
import com.arquitectura.events.EntityDeleteEventLong;

public interface DiaService extends CommonsConsumer<BaseEvent, DiaEvent, EntityDeleteEventLong> {
}
