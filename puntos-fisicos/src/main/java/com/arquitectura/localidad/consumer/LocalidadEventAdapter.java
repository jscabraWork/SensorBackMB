package com.arquitectura.localidad.consumer;


import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.LocalidadEvent;
import com.arquitectura.localidad.entity.Localidad;

public interface LocalidadEventAdapter extends EventAdapter<Localidad, LocalidadEvent> {

}
