package com.arquitectura.puntofisico.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventString;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.puntofisico.entity.PuntoFisicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class PuntoFisicoServiceConsumerImpl implements PuntoFisicoServiceConsumer {

	@Autowired
	private PuntoFisicoRepository repository;
	
	@Autowired
	private MessageService service;
	
	@Autowired
	private PuntoFisicoEventAdaparter adapter;
	
	@Override
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${puntoF.topic}'}")
	public void handleEvent(@Payload BaseEvent baseEvent,
			@Header(value = "messageId", required = true)String messageId,
			@Header(KafkaHeaders.RECEIVED_KEY)String messageKey) {
		 if (baseEvent instanceof UsuarioEvent) {
	        	handleCreateEvent((UsuarioEvent) baseEvent, messageId, messageKey);
	        } else if (baseEvent instanceof EntityDeleteEventString) {
	            handleDeleteEvent((EntityDeleteEventString) baseEvent, messageId, messageKey);
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
		 PuntoFisico puntoFisico = repository.findById(String.valueOf(event.getId())).orElse(null);
	        try {
	            if (puntoFisico == null) {
	            	puntoFisico = new PuntoFisico();
	            }
	            puntoFisico = adapter.creacion(puntoFisico, event);
	            repository.save(puntoFisico);
	        } catch (Exception ex) {
	            throw new NotRetryableException(ex);
	        }
	        try {
	          service.crearMensaje(messageId, puntoFisico.getNumeroDocumento());
	        } catch (Exception ex) {
	            throw new NotRetryableException(ex);
	        }
	}

	@Override
    @Transactional("transactionManager")
	public void handleDeleteEvent(EntityDeleteEventString eventDelete, String messageId, String messageKey) {
		if(service.existeMessage(messageId)) {
			return;
		}
        PuntoFisico puntoFisico = repository.findById(String.valueOf(eventDelete.getId())).orElse(null);
        try {
            if (puntoFisico == null) {
                return;
            }
            repository.deleteById(String.valueOf(eventDelete.getId()));
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
        	service.crearMensaje(messageId, puntoFisico.getNumeroDocumento());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
		
	}
}
