package com.arquitectura.orden.orden_promotor;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.OrdenPromotorEvent;
import com.arquitectura.orden_promotor.entity.OrdenPromotor;

public interface OrdenPromotorEventAdapter extends EventAdapter<OrdenPromotor, OrdenPromotorEvent> {
}
