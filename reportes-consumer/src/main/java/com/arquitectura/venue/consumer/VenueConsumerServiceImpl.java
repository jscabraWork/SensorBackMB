package com.arquitectura.venue.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.VenueEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.venue.entity.Venue;
import com.arquitectura.venue.entity.VenueRepository;
import com.arquitectura.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VenueConsumerServiceImpl implements VenueConsumerService {

    @Autowired
    private VenueRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private VenueEventAdapter adapter;

    @Override
    @Transactional
    @KafkaListener(topics = "#{'${venues.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                           @Header(value = "messageId", required = true) String messageId,
                           @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        if (baseEvent instanceof VenueEvent) {
            handleCreateEvent((VenueEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }
    }

    @Transactional
    @Override
    public void handleCreateEvent(VenueEvent event, String messageId, String messageKey) {
        if (service.existeMessage(messageId)) {
            return;
        }
        Venue venue = repository.findById(event.getId()).orElse(null);
        try {
            if (venue == null) {
                venue = new Venue();
            }
            venue = adapter.creacion(venue, event);
            repository.save(venue);
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, venue.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }

    @Transactional
    @Override
    public void handleDeleteEvent(EntityDeleteEventLong eventDelete, String messageId, String messageKey) {
        if(service.existeMessage(messageId)) {
            return;
        }
        Venue venue = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (venue == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, venue.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }
}
