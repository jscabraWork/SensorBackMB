package com.arquitectura.orden.orden_puntofisico;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenPuntoFisicoEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisicoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrdenConsumerPuntoFisicoServiceImpl implements OrdenConsumerPuntoFisicoService {

    @Autowired
    private OrdenPuntoFisicoRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private OrdenPuntoFisicoEventAdapter adapter;


    @Override
    @Transactional
    @KafkaListener(topics = "#{'${ordenes.puntosF.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                            @Header(value = "messageId", required = true) String messageId,
                            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey){

        if (baseEvent instanceof OrdenPuntoFisicoEvent) {
            handleCreateEvent((OrdenPuntoFisicoEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }

    }

    @Transactional
    @Override
    public void handleCreateEvent(OrdenPuntoFisicoEvent event, String messageId, String messageKey) {

        if (service.existeMessage(messageId)) {
            return;
        }
        OrdenPuntoFisico orden = repository.findById(event.getId()).orElse(null);
        try {
            if (orden == null) {
                orden = new OrdenPuntoFisico();
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

    @Transactional
    @Override
    public void handleDeleteEvent(EntityDeleteEventLong eventDelete, String messageId, String messageKey) {

        if(service.existeMessage(messageId)) {
            return;
        }
        OrdenPuntoFisico orden = repository.findById(eventDelete.getId()).orElse(null);
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
