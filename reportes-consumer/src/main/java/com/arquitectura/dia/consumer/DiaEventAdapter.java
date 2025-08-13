package com.arquitectura.dia.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.DiaEvent;
import com.arquitectura.dia.entity.Dia;

public interface DiaEventAdapter extends EventAdapter<Dia, DiaEvent> {
}
