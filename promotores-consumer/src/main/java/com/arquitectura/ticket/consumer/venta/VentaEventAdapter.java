package com.arquitectura.ticket.consumer.venta;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.TicketEvent;
import com.arquitectura.ticket.entity.Ticket;

public interface VentaEventAdapter extends EventAdapter<Ticket, TicketEvent>{

}
