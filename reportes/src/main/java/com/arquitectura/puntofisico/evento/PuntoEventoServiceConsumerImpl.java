package com.arquitectura.puntofisico.evento;
import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.EventoVendedorEvent;
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
public class PuntoEventoServiceConsumerImpl implements PuntoEventoServiceConsumer {


	@Autowired
	private PuntoFisicoRepository repository;
	
	@Autowired
	private MessageService service;
	
	@Autowired
	private PuntoEventoEventAdaparter adapter;

	
	@Override
    @Transactional("transactionManager")
	@KafkaListener(topics = "#{'${eventos-puntosfisicos.topic}'}")
	public void handleEvent(@Payload BaseEvent baseEvent,
			@Header(value = "messageId", required = true)String messageId,
			@Header(KafkaHeaders.RECEIVED_KEY)String messageKey) {
		 if (baseEvent instanceof EventoVendedorEvent) {
	        	handleCreateEvent((EventoVendedorEvent) baseEvent, messageId, messageKey);
	        } else if (baseEvent instanceof EntityDeleteEventLong) {
	            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
	        } else {
	            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
	        }
		
	}

	@Override
    @Transactional("transactionManager")
	public void handleCreateEvent(EventoVendedorEvent event, String messageId, String messageKey) {
		if (service.existeMessage(messageId)) {
            return;
        }
		 PuntoFisico puntoFisico = repository.findById(event.getNumeroDocumento()).orElse(null);
	        try {
	            if (puntoFisico == null) {
	            	return;
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
	public void handleDeleteEvent(EntityDeleteEventLong eventDelete, String messageId, String messageKey) {
	     throw new NotRetryableException("Unsupported event type: " + eventDelete.getClass().getName());
		
	}
}
