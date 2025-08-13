package com.arquitectura.transaccion.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.TransaccionEvent;
import com.arquitectura.transaccion.entity.Transaccion;

public interface TransaccionEventAdapter extends EventAdapter<Transaccion, TransaccionEvent> {
}
