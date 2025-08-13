package com.arquitectura.transaccion.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TransaccionEvent;

public interface TransaccionServiceConsumer extends CommonsConsumer<BaseEvent, TransaccionEvent, EntityDeleteEventLong> {
}
