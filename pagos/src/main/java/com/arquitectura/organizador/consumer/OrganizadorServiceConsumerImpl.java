package com.arquitectura.organizador.consumer;
import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.organizador.entity.Organizador;
import com.arquitectura.organizador.entity.OrganizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrganizadorServiceConsumerImpl implements OrganizadorServiceConsumer{

	@Autowired
	private OrganizadorRepository repository;
	
	@Autowired
	private MessageService service;
	
	@Autowired
	private OrganizadorEventAdaparter adapter;
	
	@Override
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${organizador.topic}'}")
	public void handleEvent(@Payload BaseEvent baseEvent,
			@Header(value = "messageId", required = true)String messageId,
			@Header(KafkaHeaders.RECEIVED_KEY)String messageKey) {
		 if (baseEvent instanceof UsuarioEvent) {
	        	handleCreateEvent((UsuarioEvent) baseEvent, messageId, messageKey);
	        } else if (baseEvent instanceof EntityDeleteEventLong) {
	            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
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
		 Organizador organizador = repository.findById(event.getId()).orElse(null);
	        try {
	            if (organizador == null) {
	            	organizador = new Organizador();
	            }
	            organizador = adapter.creacion(organizador, event);
	            repository.save(organizador);
	        } catch (Exception ex) {
	            throw new NotRetryableException(ex);
	        }
	        try {
	          service.crearMensaje(messageId, organizador.getNumeroDocumento());
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
        Organizador organizador = repository.findById(eventDelete.getId().toString()).orElse(null);
        try {
            if (organizador == null) {
                return;
            }
            repository.deleteById(eventDelete.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
        	service.crearMensaje(messageId, organizador.getNumeroDocumento());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
		
	}

}
