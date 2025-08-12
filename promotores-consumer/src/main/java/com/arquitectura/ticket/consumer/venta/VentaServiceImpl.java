package com.arquitectura.ticket.consumer.venta;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TicketEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VentaServiceImpl implements VentaService {

	@Autowired
	private TicketRepository repository;

	@Autowired
	private MessageService service;
	
	@Autowired
	private VentaEventAdapter adapter;
	
	
	@Override
	@Transactional
	@KafkaListener(topics = "#{'${tickets.topic}'}")
	public void handleEvent(@Payload BaseEvent baseEvent,
            @Header(value = "messageId", required = true) String messageId,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
		
	    if (baseEvent instanceof TicketEvent) {
        	handleCreateEvent((TicketEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }
		
	}

	@Transactional
	@Override
	public void handleCreateEvent(TicketEvent event, String messageId, String messageKey) {
		
		
		  if (service.existeMessage(messageId)) {
	            return;
	      }
	        try {
		        Ticket ticket = repository.findById(event.getId()).orElse(null);

				//En este caso si el ticket no existe no se debe hacer nada
		        if(ticket==null) {
					return;
				}
		        
	            ticket = adapter.creacion(ticket, event);

	            repository.save(ticket);

	            service.crearMensaje(messageId, ticket.getId().toString());

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
        Ticket ticket = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if (ticket == null) {
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
        	service.crearMensaje(messageId, ticket.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
		
	}

}
