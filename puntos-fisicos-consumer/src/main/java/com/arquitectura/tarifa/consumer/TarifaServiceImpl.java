package com.arquitectura.tarifa.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TarifaEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.entity.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TarifaServiceImpl implements TarifaService {

    @Autowired
    private TarifaRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private TarifaEventAdapter adapter;

    @Override
    @Transactional
    @KafkaListener(topics = "#{'${tarifas.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                           @Header(value = "messageId", required = true) String messageId,
                           @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        if (baseEvent instanceof TarifaEvent) {
            handleCreateEvent((TarifaEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }
    }

    @Transactional
    @Override
    public void handleCreateEvent(TarifaEvent event, String messageId, String messageKey) {
        if (service.existeMessage(messageId)) {
            return;
        }
        Tarifa tarifa = repository.findById(event.getId()).orElse(null);
        try {
            if (tarifa == null) {
                tarifa = new Tarifa();
            }
            tarifa = adapter.creacion(tarifa, event);
            repository.save(tarifa);
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
        Tarifa tarifa = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (tarifa == null) {
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
