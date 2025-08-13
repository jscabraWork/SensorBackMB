package com.arquitectura.ciudad.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.CiudadEvent;
import com.arquitectura.ciudad.entity.Ciudad;

public interface CiudadEventAdapter extends EventAdapter<Ciudad, CiudadEvent> {
}
