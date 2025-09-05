package com.arquitectura.orden_traspaso;
import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenTraspasoEvent;

public interface OrdenTraspasoConsumerService extends CommonsConsumer<BaseEvent, OrdenTraspasoEvent, EntityDeleteEventLong> {
}
