package com.arquitectura.orden_traspaso;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.OrdenPuntoFisicoEvent;
import com.arquitectura.events.OrdenTraspasoEvent;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import com.arquitectura.orden_traspaso.entity.OrdenTraspaso;

public interface OrdenTraspasoEventAdapter extends EventAdapter<OrdenTraspaso, OrdenTraspasoEvent> {



}
