package com.arquitectura.imagen.consumer;


import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.ImagenEvent;

public interface ImagenService extends CommonsConsumer<BaseEvent, ImagenEvent, EntityDeleteEventLong> {

}
