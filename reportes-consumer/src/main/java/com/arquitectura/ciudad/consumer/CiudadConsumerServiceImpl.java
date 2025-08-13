package com.arquitectura.ciudad.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.CiudadEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.ciudad.entity.Ciudad;
import com.arquitectura.ciudad.entity.CiudadRepository;
import com.arquitectura.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class CiudadConsumerServiceImpl implements CiudadConsumerService {

    @Autowired
    private CiudadRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private CiudadEventAdapter adapter;
    @Override
    @Transactional
    @KafkaListener(topics = "#{'${ciudades.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                            @Header(value = "messageId", required = true) String messageId,
                            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        if (baseEvent instanceof CiudadEvent) {
            handleCreateEvent((CiudadEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }


    }

    @Transactional
    @Override
    public void handleCreateEvent(CiudadEvent event, String messageId, String messageKey) {
        if (service.existeMessage(messageId)) {
            return;
        }
        Ciudad ciudad = repository.findById(event.getId()).orElse(null);
        try {
            if (ciudad == null) {
                ciudad = new Ciudad();
            }
            ciudad = adapter.creacion(ciudad, event);
            repository.save(ciudad);
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
        Ciudad ciudad = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (ciudad == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, ciudad.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }

    }

}
