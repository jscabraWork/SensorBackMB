package com.arquitectura.temporada.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TemporadaEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.temporada.entity.Temporada;
import com.arquitectura.temporada.entity.TemporadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TemporadaConsumerServiceImpl implements TemporadaConsumerService {

    @Autowired
    private TemporadaRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private TemporadaEventAdapter adapter;

    @Override
    @Transactional
    @KafkaListener(topics = "#{'${temporadas.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                            @Header(value = "messageId", required = true) String messageId,
                            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        if (baseEvent instanceof TemporadaEvent) {
            handleCreateEvent((TemporadaEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }

    }

    @Transactional
    @Override
    public void handleCreateEvent(TemporadaEvent event, String messageId, String messageKey) {

        if (service.existeMessage(messageId)) {
            return;
        }
        Temporada temporada = repository.findById(event.getId()).orElse(null);
        try {
            if (temporada == null) {
                temporada = new Temporada();
            }
            temporada = adapter.creacion(temporada, event);
            repository.save(temporada);
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, temporada.getId().toString());
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
        Temporada temporada = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (temporada == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, temporada.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }

    }
}
