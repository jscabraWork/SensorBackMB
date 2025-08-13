package com.arquitectura.imagen.consumer;
import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.ImagenEvent;
import com.arquitectura.imagen.entity.Imagen;
import com.arquitectura.imagen.entity.ImagenRepository;
import com.arquitectura.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ImagenConsumerServiceImpl implements ImagenConsumerService{

	@Autowired
	private ImagenRepository repository;
	

	@Autowired
	private MessageService service;
	
	@Autowired
	private ImagenEventAdapter adapter;
	
	
	@Override
	@Transactional("transactionManager")
	@KafkaListener(topics = "#{'${imagenes.topic}'}")
	public void handleEvent(@Payload BaseEvent baseEvent,
			@Header(value = "messageId", required = true) String messageId,
			@Header(KafkaHeaders.RECEIVED_KEY)String messageKey) {
	    if (baseEvent instanceof ImagenEvent) {
        	handleCreateEvent((ImagenEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }
		
	}

	@Transactional("transactionManager")
	@Override
	public void handleCreateEvent(ImagenEvent event, String messageId, String messageKey) {

		  if (service.existeMessage(messageId)) {
	            return;
	        }
	        Imagen imagen = repository.findById(event.getId()).orElse(null);
	        try {
	            if (imagen == null) {
	            	imagen = new Imagen();
	            }
	            imagen = adapter.creacion(imagen, event);
	            repository.save(imagen);
	        } catch (Exception ex) {
	            throw new NotRetryableException(ex);
	        }
	        try {
	          service.crearMensaje(messageId, imagen.getId().toString());
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
        Imagen imagen = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (imagen == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
        	service.crearMensaje(messageId, imagen.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
		
	}

}
