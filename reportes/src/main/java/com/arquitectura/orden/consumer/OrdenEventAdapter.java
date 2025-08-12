package com.arquitectura.orden.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.OrdenEvent;
import com.arquitectura.orden.entity.Orden;

public interface OrdenEventAdapter extends EventAdapter<Orden, OrdenEvent> {
}
