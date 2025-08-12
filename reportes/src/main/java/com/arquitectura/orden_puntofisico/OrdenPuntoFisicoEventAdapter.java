package com.arquitectura.orden_puntofisico;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.OrdenPuntoFisicoEvent;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;

public interface OrdenPuntoFisicoEventAdapter extends EventAdapter<OrdenPuntoFisico, OrdenPuntoFisicoEvent> {
}
