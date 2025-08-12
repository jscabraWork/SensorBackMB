package com.arquitectura.servicio.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.ServicioEvent;
import com.arquitectura.servicio.entity.Servicio;

public interface ServicioEventAdapter extends EventAdapter<Servicio, ServicioEvent> {
}
