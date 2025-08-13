package com.arquitectura.localidad.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.entity.DiaRepository;
import com.arquitectura.events.LocalidadEvent;
import com.arquitectura.localidad.entity.Localidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalidadEventAdapterImpl extends EventAdapterImpl<Localidad, LocalidadEvent> implements LocalidadEventAdapter{

	@Autowired
	private DiaRepository diasRepository;

	@Override
	public Localidad creacion(Localidad entity, LocalidadEvent event) {

		entity.setId(event.getId());
		entity.setNombre(event.getNombre());
		entity.setTipo(event.getTipo());
		entity.setAporteMinimo(event.getAporteMinimo());

		if(event.getDiasIds()!= null && !event.getDiasIds().isEmpty()) {
			List<Dia> dias = diasRepository.findAllById(event.getDiasIds());
			entity.setDias(dias);
		}

		return super.creacion(entity, event);
	}
}
