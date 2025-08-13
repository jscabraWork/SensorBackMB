package com.arquitectura.ingreso.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.IngresoEvent;
import com.arquitectura.ingreso.entity.Ingreso;
import com.arquitectura.ingreso.entity.IngresoRepository;
import com.arquitectura.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IngresoConsumerServiceImpl implements IngresoConsumerService {

    @Autowired
    private IngresoRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private IngresoEventAdapter adapter;

    @Override
    @Transactional
    @KafkaListener(topics = "#{'${ingresos.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                           @Header(value = "messageId", required = true) String messageId,
                           @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        if (baseEvent instanceof IngresoEvent) {
            handleCreateEvent((IngresoEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Tipo de evento no soportado: " + baseEvent.getClass().getName());
        }
    }

    @Transactional
    @Override
    public void handleCreateEvent(IngresoEvent event, String messageId, String messageKey) {
        if (service.existeMessage(messageId)) {
            return;
        }
        Ingreso ingreso = repository.findById(event.getId()).orElse(null);
        try {
            if (ingreso == null) {
                ingreso = new Ingreso();
            }
            ingreso = adapter.creacion(ingreso, event);
            repository.save(ingreso);
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
        if (service.existeMessage(messageId)) {
            return;
        }
        Ingreso ingreso = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (ingreso == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, eventDelete.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }
}