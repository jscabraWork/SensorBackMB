package com.arquitectura.tipo.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.TipoEvent;
import com.arquitectura.tipo.entity.Tipo;

public interface TipoEventAdapter extends EventAdapter<Tipo, TipoEvent> {
}
