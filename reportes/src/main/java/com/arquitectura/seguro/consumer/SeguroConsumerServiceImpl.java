package com.arquitectura.seguro.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.SeguroEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.seguro.entity.Seguro;
import com.arquitectura.seguro.entity.SeguroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeguroConsumerServiceImpl implements SeguroConsumerService {

    @Autowired
    private SeguroRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private SeguroEventAdapter adapter;

    @Override
    @Transactional
    @KafkaListener(topics = "#{'${seguros.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                           @Header(value = "messageId", required = true) String messageId,
                           @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        if (baseEvent instanceof SeguroEvent) {
            handleCreateEvent((SeguroEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Tipo de evento no soportado: " + baseEvent.getClass().getName());
        }
    }

    @Transactional
    @Override
    public void handleCreateEvent(SeguroEvent event, String messageId, String messageKey) {
        if (service.existeMessage(messageId)) {
            return;
        }
        Seguro seguro = repository.findById(event.getId()).orElse(null);
        try {
            if (seguro == null) {
                seguro = new Seguro();
            }
            seguro = adapter.creacion(seguro, event);
            repository.save(seguro);
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
        Seguro seguro = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (seguro == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, seguro.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }
}