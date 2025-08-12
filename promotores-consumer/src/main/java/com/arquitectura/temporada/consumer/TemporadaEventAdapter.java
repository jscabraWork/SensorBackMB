package com.arquitectura.temporada.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.TemporadaEvent;
import com.arquitectura.temporada.entity.Temporada;

public interface TemporadaEventAdapter extends EventAdapter<Temporada, TemporadaEvent> {
}
