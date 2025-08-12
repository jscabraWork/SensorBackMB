package com.arquitectura.ticket.consumer.venta;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TicketEvent;


public interface VentaService extends CommonsConsumer<BaseEvent, TicketEvent, EntityDeleteEventLong>{

}
