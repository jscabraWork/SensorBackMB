package com.arquitectura.ingreso.consumer;


import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.IngresoEvent;

public interface IngresoConsumerService extends CommonsConsumer<BaseEvent, IngresoEvent, EntityDeleteEventLong> {

}
