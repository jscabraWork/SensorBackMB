package com.arquitectura.imagen.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.ImagenEvent;
import com.arquitectura.imagen.entity.Imagen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImagenEventAdapterImpl extends EventAdapterImpl<Imagen, ImagenEvent> implements ImagenEventAdapter{

	@Autowired
	private EventoRepository repository;

	public void setRepository(EventoRepository repository) {
		this.repository = repository;
	}

	@Override
	public Imagen creacion(Imagen entity, ImagenEvent event) {
		
		entity.setId(event.getId());
		entity.setNombre(event.getNombre());
		entity.setUrl(event.getUrl());
		entity.setTipo(event.getTipo());
		entity.setEvento(repository.findById(event.getEventoId()).orElse(null));
		
		return super.creacion(entity, event);
	}
}
