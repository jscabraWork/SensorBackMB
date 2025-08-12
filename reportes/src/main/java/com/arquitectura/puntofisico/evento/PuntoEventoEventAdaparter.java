package com.arquitectura.puntofisico.evento;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.EventoVendedorEvent;
import com.arquitectura.puntofisico.entity.PuntoFisico;


public interface PuntoEventoEventAdaparter extends EventAdapter<PuntoFisico, EventoVendedorEvent> {

}
