package com.arquitectura.ingreso.consumer;


import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.IngresoEvent;
import com.arquitectura.ingreso.entity.Ingreso;

public interface IngresoEventAdapter extends EventAdapter<Ingreso, IngresoEvent> {

}
