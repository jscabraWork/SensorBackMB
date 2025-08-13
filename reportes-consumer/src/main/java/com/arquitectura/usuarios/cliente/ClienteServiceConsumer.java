package com.arquitectura.usuarios.cliente;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.EntityDeleteEventString;
import com.arquitectura.events.UsuarioEvent;

public interface ClienteServiceConsumer extends CommonsConsumer<BaseEvent, UsuarioEvent, EntityDeleteEventString> {
}
