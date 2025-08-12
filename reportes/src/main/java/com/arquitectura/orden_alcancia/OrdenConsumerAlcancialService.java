package com.arquitectura.orden_alcancia;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenAlcanciaEvent;


public interface OrdenConsumerAlcancialService extends CommonsConsumer<BaseEvent, OrdenAlcanciaEvent, EntityDeleteEventLong> {
}
