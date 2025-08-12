package com.arquitectura.localidad.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.LocalidadEvent;
import com.arquitectura.localidad.entity.Localidad;
import com.arquitectura.localidad.entity.LocalidadRepository;
import com.arquitectura.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LocalidadConsumerServiceImpl implements LocalidadConsumerService {

	@Autowired
	private LocalidadRepository repository;

	@Autowired
	private MessageService service;
	
	@Autowired
	private LocalidadEventAdapter adapter;
	
	
	@Override
	@Transactional("transactionManager")
	@KafkaListener(topics = "#{'${localidades.topic}'}")
	public void handleEvent(@Payload BaseEvent baseEvent,
            @Header(value = "messageId", required = true) String messageId,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
		
	    if (baseEvent instanceof LocalidadEvent) {
        	handleCreateEvent((LocalidadEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }

	}

	@Transactional("transactionManager")
	@Override
	public void handleCreateEvent(LocalidadEvent event, String messageId, String messageKey) {
		

		  if (service.existeMessage(messageId)) {
	            return;
	        }
	        Localidad localidad = repository.findById(event.getId()).orElse(null);
	        try {
	            if (localidad == null) {
	            	localidad = new Localidad();
	            }
	            localidad = adapter.creacion(localidad, event);
	            repository.save(localidad);
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
		if(service.existeMessage(messageId)) {
			return;
		}
        Localidad localidad = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (localidad == null) {
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

