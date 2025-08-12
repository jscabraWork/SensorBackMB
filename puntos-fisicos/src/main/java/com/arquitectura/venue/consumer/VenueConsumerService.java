package com.arquitectura.venue.consumer;

import com.arquitectura.consumer.CommonsConsumer;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.VenueEvent;

public interface VenueConsumerService extends CommonsConsumer<BaseEvent, VenueEvent, EntityDeleteEventLong> {
}
