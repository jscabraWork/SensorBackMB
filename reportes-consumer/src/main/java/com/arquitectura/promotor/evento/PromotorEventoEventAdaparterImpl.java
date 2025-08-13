package com.arquitectura.promotor.evento;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.EventoVendedorEvent;
import com.arquitectura.promotor.entity.Promotor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class PromotorEventoEventAdaparterImpl extends EventAdapterImpl<Promotor, EventoVendedorEvent> implements PromotorEventoEventAdaparter {

	@Autowired
	private EventoRepository eventoRepository;
	
	
	@Override
	public Promotor creacion(Promotor entity, EventoVendedorEvent event) {

		if(event.getEventosId()==null || event.getEventosId().isEmpty()) {
			entity.setEventos(null);
		}
		else {
			entity.setEventos(eventoRepository.findAllById(event.getEventosId()));

		}
		return super.creacion(entity, event);
	}
}
