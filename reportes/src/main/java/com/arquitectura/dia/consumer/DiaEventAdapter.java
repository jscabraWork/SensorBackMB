package com.arquitectura.dia.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.events.DiaEvent;

public interface DiaEventAdapter extends EventAdapter<Dia, DiaEvent> {
}
