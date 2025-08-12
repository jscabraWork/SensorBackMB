package com.arquitectura.orden_puntofisico;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenPuntoFisicoEvent;

public interface OrdenConsumerPuntoFisicoService extends CommonsConsumer<BaseEvent, OrdenPuntoFisicoEvent, EntityDeleteEventLong> {
}
