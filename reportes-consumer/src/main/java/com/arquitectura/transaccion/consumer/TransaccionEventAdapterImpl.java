package com.arquitectura.transaccion.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.TransaccionEvent;
import com.arquitectura.orden.entity.OrdenRepository;
import com.arquitectura.transaccion.entity.Transaccion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Adaptador de eventos para la creación de una transacción a partir de un evento de tipo {@link TransaccionEvent}.
 * Extiende de {@link EventAdapterImpl} para mapear los eventos a la entidad {@link Transaccion}.
 */
@Component
public class TransaccionEventAdapterImpl extends EventAdapterImpl<Transaccion, TransaccionEvent> implements TransaccionEventAdapter {

    @Autowired
    OrdenRepository ordenRepository;

    /**
     * Mapea un {@link TransaccionEvent} a una entidad {@link Transaccion}.
     *
     * @param entity Instancia de la entidad {@link Transaccion} a actualizar.
     * @param event Evento que contiene los datos de la transacción.
     * @return La entidad {@link Transaccion} actualizada.
     */
    public Transaccion creacion(Transaccion entity, TransaccionEvent event){
        entity.setId(event.getId());
        entity.setAmount(event.getAmount());
        entity.setEmail(event.getEmail());
        entity.setFullName(event.getFullname());
        entity.setIdPasarela(event.getIdPasarela());
        entity.setIdPersona(event.getIdPersona());
        entity.setIp(event.getIp());
        entity.setPhone(event.getPhone());
        entity.setStatus(event.getStatus());
        entity.setMetodo(event.getMetodo());
        entity.setMetodoNombre(event.getMetodoNombre());
        entity.setIdBasePasarela(event.getIdBasePasarela());
        entity.setOrden(ordenRepository.findById(event.getOrdenId()).orElse(null));
        return super.creacion(entity, event);
    }
}
