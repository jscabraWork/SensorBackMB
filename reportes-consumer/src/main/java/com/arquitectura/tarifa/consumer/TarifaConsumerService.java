package com.arquitectura.tarifa.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.TarifaEvent;
import com.arquitectura.events.EntityDeleteEventLong;

public interface TarifaConsumerService extends CommonsConsumer<BaseEvent, TarifaEvent, EntityDeleteEventLong> {
}
