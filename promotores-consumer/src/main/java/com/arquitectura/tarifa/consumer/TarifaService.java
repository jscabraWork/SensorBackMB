package com.arquitectura.tarifa.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TarifaEvent;

public interface TarifaService extends CommonsConsumer<BaseEvent, TarifaEvent, EntityDeleteEventLong> {
}
