package com.arquitectura.puntofisico.evento;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.EventoVendedorEvent;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.puntofisico.entity.PuntoFisicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class PuntoEventoEventAdaparterImpl extends EventAdapterImpl<PuntoFisico, EventoVendedorEvent> implements PuntoEventoEventAdaparter{

	@Autowired
	private EventoRepository eventoRepository;
	
	
	@Override
	public PuntoFisico creacion(PuntoFisico entity, EventoVendedorEvent event) {

		if(event.getEventosId()==null || event.getEventosId().isEmpty()) {
			entity.setEventos(null);
		}
		else {
			entity.setEventos(eventoRepository.findAllById(event.getEventosId()));

		}
		return super.creacion(entity, event);
	}
}
