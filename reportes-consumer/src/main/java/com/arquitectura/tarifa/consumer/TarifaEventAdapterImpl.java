package com.arquitectura.tarifa.consumer;

import com.arquitectura.adapter.EventAdapterImpl;
import com.arquitectura.events.TarifaEvent;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.entity.LocalidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TarifaEventAdapterImpl extends EventAdapterImpl<Tarifa, TarifaEvent> implements TarifaEventAdapter {

    @Autowired
    private LocalidadRepository localidadRepository;

    @Override
    public Tarifa creacion(Tarifa entity, TarifaEvent event) {

        entity.setId(event.getId());
        entity.setNombre(event.getNombre());
        entity.setPrecio(event.getPrecio());
        entity.setServicio(event.getServicio());
        entity.setIva(event.getIva());
        entity.setEstado(event.getEstado());

        if(event.getLocalidadId() != null) {
            Localidad localidad = localidadRepository.findById(event.getLocalidadId()).orElse(null);
            entity.setLocalidad(localidad);
        }

        return super.creacion(entity, event);
    }
}
