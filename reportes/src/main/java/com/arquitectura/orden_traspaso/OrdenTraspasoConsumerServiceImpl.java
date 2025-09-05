package com.arquitectura.orden_traspaso;
import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenTraspasoEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.orden_traspaso.entity.OrdenTraspaso;
import com.arquitectura.orden_traspaso.entity.OrdenTraspasoRepository;
import com.arquitectura.transaccion.consumer.TransaccionEventAdapter;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.entity.TransaccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrdenTraspasoConsumerServiceImpl implements OrdenTraspasoConsumerService {

    @Autowired
    private OrdenTraspasoRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private OrdenTraspasoEventAdapter adapter;

    @Autowired
    private TransaccionEventAdapter transaccionAdapter;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Override
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${ordenes.traspaso.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                            @Header(value = "messageId", required = true) String messageId,
                            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey){

        if (baseEvent instanceof OrdenTraspasoEvent) {
            handleCreateEvent((OrdenTraspasoEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }

    }

    @Transactional("transactionManager")
    @Override
    public void handleCreateEvent(OrdenTraspasoEvent event, String messageId, String messageKey) {

        if (service.existeMessage(messageId)) {
            return;
        }
        OrdenTraspaso orden = repository.findById(event.getId()).orElse(null);
        try {
            if (orden == null) {
                orden = new OrdenTraspaso();
            }
            orden = adapter.creacion(orden, event);
            repository.save(orden);

            //Guardar transacción
            Transaccion transaccion = transaccionRepository.findById(event.getTransaccion().getId()).orElse(null);
            if(transaccion ==null){
                transaccion = new Transaccion();
            }
            transaccion =  transaccionAdapter.creacion(transaccion, event.getTransaccion());
            transaccionRepository.save(transaccion);

        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, orden.getId().toString());
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
        OrdenTraspaso orden = repository.findById(eventDelete.getId()).orElse(null);
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
