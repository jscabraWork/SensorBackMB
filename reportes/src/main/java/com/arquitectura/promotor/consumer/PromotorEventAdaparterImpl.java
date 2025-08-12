package com.arquitectura.promotor.consumer;


import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.promotor.entity.Promotor;
import org.springframework.stereotype.Component;


@Component
public class PromotorEventAdaparterImpl extends EventAdapterImpl<Promotor, UsuarioEvent> implements PromotorEventAdaparter{

	@Override
	public Promotor creacion(Promotor entity, UsuarioEvent event) {
		
		entity.setNombre(event.getNombre());
		entity.setNumeroDocumento(event.getId());
		entity.setCorreo(event.getCorreo());
		entity.setCelular(event.getCelular());
		entity.setTipoDocumento(event.getTipoDocumento());
		return super.creacion(entity, event);
	}
}
