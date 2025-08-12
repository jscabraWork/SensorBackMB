package com.arquitectura.ciudad.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.CiudadEvent;
import com.arquitectura.events.EntityDeleteEventLong;

public interface CiudadService extends CommonsConsumer<BaseEvent, CiudadEvent, EntityDeleteEventLong> {
}
