package com.arquitectura.cliente.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.tipo_documento.TipoDocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementación del adaptador que convierte un evento {@link UsuarioEvent} en una entidad {@link Cliente}.
 * Este adaptador es responsable de mapear los datos del evento a la entidad correspondiente.
 */
@Component
public class ClienteEventAdaparterImpl extends EventAdapterImpl<Cliente, UsuarioEvent> implements ClienteEventAdapter {


    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    /**
     * Metdo para mapear los datos del evento {@link UsuarioEvent} a una entidad {@link Cliente}.
     * Si el cliente no existe, se crea uno nuevo con los datos del evento.
     *
     * @param entity Entidad cliente que se va a actualizar o crear.
     * @param event Evento que contiene los datos del cliente a mapear.
     * @return La entidad {@link Cliente} actualizada con los datos del evento.
     */
    @Override
    public Cliente creacion(Cliente entity, UsuarioEvent event) {
        entity.setNumeroDocumento(event.getId());
        entity.setNombre(event.getNombre());
        entity.setCorreo(event.getCorreo());
        entity.setCelular(event.getCelular());
        entity.setTipoDocumento(tipoDocumentoRepository.findById(event.getTipoDocumentoId()).orElse(null));
        return super.creacion(entity, event);
    }
}
