package com.arquitectura.servicio.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.ServicioEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.servicio.entity.Servicio;
import com.arquitectura.servicio.entity.ServicioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class ServicioConsumerServiceImpl implements ServicioConsumerService {

    @Autowired
    private ServicioRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private ServicioEventAdapter adapter;

    @Override
    @Transactional
    @KafkaListener(topics = "#{'${servicios.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                            @Header(value = "messageId", required = true) String messageId,
                            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey){

        if (baseEvent instanceof ServicioEvent) {
            handleCreateEvent((ServicioEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }

    }

    @Transactional
    @Override
    public void handleCreateEvent(ServicioEvent event, String messageId, String messageKey) {

        if (service.existeMessage(messageId)) {
            return;
        }
        Servicio servicio = repository.findById(event.getId()).orElse(null);
        try {
            if (servicio == null) {
                servicio = new Servicio();
            }
            servicio = adapter.creacion(servicio, event);
            repository.save(servicio);
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, servicio.getId().toString());
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
        Servicio servicio = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (servicio == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }

        try {
            service.crearMensaje(messageId, servicio.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }
}
