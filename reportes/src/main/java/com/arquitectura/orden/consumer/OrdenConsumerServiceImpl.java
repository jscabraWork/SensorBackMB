package com.arquitectura.orden.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.entity.OrdenRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrdenConsumerServiceImpl implements OrdenConsumerService {

    @Autowired
    private OrdenRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private OrdenEventAdapter adapter;


    @Override
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${ordenes.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                            @Header(value = "messageId", required = true) String messageId,
                            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey){

        if (baseEvent instanceof OrdenEvent) {
            handleCreateEvent((OrdenEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }

    }

    @Transactional("transactionManager")
    @Override
    public void handleCreateEvent(OrdenEvent event, String messageId, String messageKey) {

        if (service.existeMessage(messageId)) {
            return;
        }
        Orden orden = repository.findById(event.getId()).orElse(null);
        try {
            if (orden == null) {
                orden = new Orden();
            }
            orden = adapter.creacion(orden, event);
            repository.save(orden);
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, orden.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }

    @Transactional("transactionManager")
    @Override
    public void handleDeleteEvent(EntityDeleteEventLong eventDelete, String messageId, String messageKey) {

        if(service.existeMessage(messageId)) {
            return;
        }
        Orden orden = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (orden == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }

        try {
            service.crearMensaje(messageId, orden.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }
}
