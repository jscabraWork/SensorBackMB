package com.arquitectura.ticket.consumer;

import com.arquitectura.adapter.EventAdapter;
import com.arquitectura.events.TicketPromotorEvent;
import com.arquitectura.ticket.entity.Ticket;

public interface TicketEventAdapter extends EventAdapter<Ticket, TicketPromotorEvent> {
}
