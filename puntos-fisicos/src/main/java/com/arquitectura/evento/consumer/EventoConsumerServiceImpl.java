package com.arquitectura.evento.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.EventoEvent;
import com.arquitectura.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EventoConsumerServiceImpl implements EventoConsumerService {

    @Autowired
    private EventoRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private EventoEventAdapter adapter;

    @Override
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${eventos.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                           @Header(value = "messageId", required = true) String messageId,
                           @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        if (baseEvent instanceof EventoEvent) {
            handleCreateEvent((EventoEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }
    }

    @Transactional
    @Override
    public void handleCreateEvent(EventoEvent event, String messageId, String messageKey) {
        if (service.existeMessage(messageId)) {
            return;
        }
        Evento evento = repository.findById(event.getId()).orElse(null);
        try {
            if (evento == null) {
                evento = new Evento();
            }
            evento = adapter.creacion(evento, event);
            repository.save(evento);
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, event.getId().toString());
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
        Evento evento = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (evento == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, evento.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }
}
