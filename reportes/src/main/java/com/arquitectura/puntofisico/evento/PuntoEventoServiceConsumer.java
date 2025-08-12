package com.arquitectura.puntofisico.evento;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.EventoVendedorEvent;

public interface PuntoEventoServiceConsumer extends CommonsConsumer<BaseEvent, EventoVendedorEvent, EntityDeleteEventLong>{

}
