package com.arquitectura.promotor.consumer;


import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.UsuarioEvent;

public interface PromotorServiceConsumer extends CommonsConsumer<BaseEvent, UsuarioEvent, EntityDeleteEventLong> {

}
