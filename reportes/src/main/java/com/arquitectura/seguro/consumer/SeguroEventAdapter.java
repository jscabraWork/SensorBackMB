package com.arquitectura.seguro.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.SeguroEvent;
import com.arquitectura.seguro.entity.Seguro;

public interface SeguroEventAdapter extends EventAdapter<Seguro, SeguroEvent> {
}
