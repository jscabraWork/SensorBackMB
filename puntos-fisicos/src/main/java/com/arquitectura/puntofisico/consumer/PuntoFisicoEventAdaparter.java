package com.arquitectura.puntofisico.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.puntofisico.entity.PuntoFisico;

public interface PuntoFisicoEventAdaparter extends EventAdapter<PuntoFisico, UsuarioEvent> {

}
