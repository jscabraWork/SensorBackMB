package com.arquitectura.promotor.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.EntityDeleteEventString;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.promotor.entity.PromotorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class PromotorServiceConsumerImpl implements PromotorServiceConsumer{

	@Autowired
	private PromotorRepository repository;
	
	@Autowired
	private MessageService service;
	
	@Autowired
	private PromotorEventAdaparter adapter;
	
	@Override
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${promotor.topic}'}")
	public void handleEvent(@Payload BaseEvent baseEvent,
			@Header(value = "messageId", required = true)String messageId,
			@Header(KafkaHeaders.RECEIVED_KEY)String messageKey) {
		 if (baseEvent instanceof UsuarioEvent) {
	        	handleCreateEvent((UsuarioEvent) baseEvent, messageId, messageKey);
	        } else if (baseEvent instanceof EntityDeleteEventLong) {
	            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
	        } else if (baseEvent instanceof EntityDeleteEventString) {
	            handleDeleteEventString((EntityDeleteEventString) baseEvent, messageId, messageKey);
	        } else {
	            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
	        }
		
	}

	@Override
    @Transactional("transactionManager")
	public void handleCreateEvent(UsuarioEvent event, String messageId, String messageKey) {
		if (service.existeMessage(messageId)) {
            return;
        }
		 Promotor promotor = repository.findById(String.valueOf(event.getId())).orElse(null);
	        try {
	            if (promotor == null) {
	            	promotor = new Promotor();
	            }
	            promotor = adapter.creacion(promotor, event);
	            repository.save(promotor);
	        } catch (Exception ex) {
	            throw new NotRetryableException(ex);
	        }
	        try {
	          service.crearMensaje(messageId, promotor.getNumeroDocumento());
	        } catch (Exception ex) {
	            throw new NotRetryableException(ex);
	        }
	}

	@Override
    @Transactional("transactionManager")
	public void handleDeleteEvent(EntityDeleteEventLong eventDelete, String messageId, String messageKey) {
		if(service.existeMessage(messageId)) {
			return;
		}
        Promotor promotor = repository.findById(String.valueOf(eventDelete.getId())).orElse(null);
        try {
            if (promotor == null) {
                return;
            }
            repository.deleteById(String.valueOf(eventDelete.getId()));
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
        	service.crearMensaje(messageId, promotor.getNumeroDocumento());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
		
	}

	@Transactional("transactionManager")
	public void handleDeleteEventString(EntityDeleteEventString eventDelete, String messageId, String messageKey) {
		if(service.existeMessage(messageId)) {
			return;
		}
        Promotor promotor = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (promotor == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
        	service.crearMensaje(messageId, promotor.getNumeroDocumento());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
	}
}
