package com.arquitectura.promotor.evento;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.EventoVendedorEvent;

public interface PromotorEventoServiceConsumer extends CommonsConsumer<BaseEvent, EventoVendedorEvent, EntityDeleteEventLong>{

}
