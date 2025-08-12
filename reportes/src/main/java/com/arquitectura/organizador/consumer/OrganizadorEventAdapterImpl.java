package com.arquitectura.organizador.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.organizador.entity.Organizador;
import org.springframework.stereotype.Component;

/**
 * Adaptador para eventos de {@link UsuarioEvent} que convierte los datos del evento en una entidad {@link Organizador}.
 */
@Component
public class OrganizadorEventAdapterImpl extends EventAdapterImpl<Organizador, UsuarioEvent> implements OrganizadorEventAdapter {

	/**
	 * Crea una nueva instancia de {@link Organizador} a partir de un {@link UsuarioEvent}.
	 *
	 * @param entity Entidad {@link Organizador} a crear.
	 * @param event Evento que contiene los datos del organizador.
	 * @return La entidad {@link Organizador} creada.
	 */
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
