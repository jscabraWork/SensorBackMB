package com.arquitectura.tipo.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TipoEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.tipo.entity.TipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TipoConsumerServiceImpl implements TipoConsumerService {

    @Autowired
    private TipoRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private TipoEventAdapter adapter;

    @Override
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${tipos-eventos.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                            @Header(value = "messageId", required = true) String messageId,
                            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        if (baseEvent instanceof TipoEvent) {
            handleCreateEvent((TipoEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }
    }

    @Transactional("transactionManager")
    @Override
    public void handleCreateEvent(TipoEvent event, String messageId, String messageKey) {
        if (service.existeMessage(messageId)) {
            return;
        }
        Tipo tipo = repository.findById(event.getId()).orElse(null);
        try {
            if (tipo == null) {
                tipo = new Tipo();
            }
            tipo = adapter.creacion(tipo, event);
            repository.save(tipo);
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, event.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }

    @Transactional("transactionManager")
    @Override
    public void handleDeleteEvent(EntityDeleteEventLong eventDelete, String messageId, String messageKey) {
        if (service.existeMessage(messageId)) {
            return;
        }
        Tipo tipo = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (tipo == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, tipo.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }
}
