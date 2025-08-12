package com.arquitectura.orden_promotor;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenPromotorEvent;

public interface OrdenConsumerPromotorService extends CommonsConsumer<BaseEvent, OrdenPromotorEvent, EntityDeleteEventLong> {
}
