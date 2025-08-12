package com.arquitectura.promotor.evento;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.EventoVendedorEvent;
import com.arquitectura.promotor.entity.Promotor;


public interface PromotorEventoEventAdaparter extends EventAdapter<Promotor, EventoVendedorEvent> {

}
