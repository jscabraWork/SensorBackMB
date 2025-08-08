package com.arquitectura.cliente;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.UsuarioEvent;

public interface ClienteServiceConsumer extends CommonsConsumer<BaseEvent, UsuarioEvent, EntityDeleteEventLong> {
}
