package com.arquitectura.ticket.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TicketPromotorEvent;

public interface TicketConsumerService extends CommonsConsumer<BaseEvent, TicketPromotorEvent, EntityDeleteEventLong> {
}
