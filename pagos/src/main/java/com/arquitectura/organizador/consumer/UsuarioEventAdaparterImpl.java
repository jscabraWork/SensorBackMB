package com.arquitectura.organizador.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.organizador.entity.Organizador;
import org.springframework.stereotype.Component;

@Component
public class UsuarioEventAdaparterImpl extends EventAdapterImpl<Organizador, UsuarioEvent> implements UsuarioEventAdaparter{

	@Override
	public Organizador creacion(Organizador entity, UsuarioEvent event) {
		entity.setNombre(event.getNombre());
		entity.setNumeroDocumento(event.getId());
		entity.setCelular(event.getCelular());
		entity.setCorreo(event.getCorreo());
		entity.setTipoDocumento(event.getTipoDocumento());
		return super.creacion(entity, event);
	}
}
