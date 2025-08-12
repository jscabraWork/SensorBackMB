package com.arquitectura.tarifa.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.TarifaEvent;
import com.arquitectura.tarifa.entity.Tarifa;

public interface TarifaEventAdapter extends EventAdapter<Tarifa, TarifaEvent> {
}
