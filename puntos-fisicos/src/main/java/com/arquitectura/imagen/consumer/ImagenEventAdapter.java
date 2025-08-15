package com.arquitectura.imagen.consumer;


import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.ImagenEvent;
import com.arquitectura.imagen.entity.Imagen;

public interface ImagenEventAdapter extends EventAdapter<Imagen, ImagenEvent> {

}
