package com.arquitectura.ciudad.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.events.CiudadEvent;

public interface CiudadEventAdapter extends EventAdapter<Ciudad, CiudadEvent> {
}
