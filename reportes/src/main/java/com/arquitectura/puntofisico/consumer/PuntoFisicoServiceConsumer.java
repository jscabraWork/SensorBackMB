package com.arquitectura.puntofisico.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventString;
import com.arquitectura.events.UsuarioEvent;

public interface PuntoFisicoServiceConsumer extends CommonsConsumer<BaseEvent, UsuarioEvent, EntityDeleteEventString> {

}
