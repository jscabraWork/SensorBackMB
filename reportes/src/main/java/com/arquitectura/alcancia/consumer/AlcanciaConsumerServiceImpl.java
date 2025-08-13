package com.arquitectura.alcancia.consumer;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.alcancia.entity.AlcanciaRepository;
import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.AlcanciaEvent;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AlcanciaConsumerServiceImpl implements AlcanciaConsumerService {

	@Autowired
	private AlcanciaRepository repository;
	
	@Autowired
	private MessageService service;
	
	@Autowired
	private AlcanciaEventAdapter adapter;
	
	@Override
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${alcancias.topic}'}")
	  public void handleEvent(@Payload BaseEvent baseEvent,
              @Header(value = "messageId", required = true) String messageId,
              @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) { 
		
        if (baseEvent instanceof AlcanciaEvent) {
        	handleCreateEvent((AlcanciaEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }
		
		
	}

	@Transactional("transactionManager")
	@Override
	public void handleCreateEvent(AlcanciaEvent event, String messageId, String messageKey) {

        System.out.println("EVENTO RECIBIDO: " + event);

        if (service.existeMessage(messageId)) {
            return;
        }
        Alcancia alcancia = repository.findById(event.getId()).orElse(null);
        try {
            if (alcancia == null) {
            	alcancia = new Alcancia();
            }
            alcancia = adapter.creacion(alcancia, event);
            repository.saveAndFlush(alcancia);
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
          service.crearMensaje(messageId, alcancia.getId().toString());
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
        Alcancia alcancia = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (alcancia == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
        	service.crearMensaje(messageId, alcancia.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
		
	}

}
