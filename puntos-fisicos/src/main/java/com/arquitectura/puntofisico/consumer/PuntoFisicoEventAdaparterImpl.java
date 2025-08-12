package com.arquitectura.puntofisico.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import org.springframework.stereotype.Component;


@Component
public class PuntoFisicoEventAdaparterImpl extends EventAdapterImpl<PuntoFisico, UsuarioEvent> implements PuntoFisicoEventAdaparter {

	@Override
	public PuntoFisico creacion(PuntoFisico entity, UsuarioEvent event) {
		
		entity.setNombre(event.getNombre());
		entity.setNumeroDocumento(event.getId());
		entity.setCorreo(event.getCorreo());
		entity.setCelular(event.getCelular());
		entity.setTipoDocumento(event.getTipoDocumento());
		return super.creacion(entity, event);
	}
}
