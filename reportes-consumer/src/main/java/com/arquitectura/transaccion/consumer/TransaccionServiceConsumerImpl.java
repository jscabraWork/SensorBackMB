package com.arquitectura.transaccion.consumer;

import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TransaccionEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.entity.TransaccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consome eventos relacionados a las transacciones y actualiza la base de datos en consecuencia.
 * Escucha los eventos de creación/modificación y eliminación de transacciones a través de Kafka.
 */
@Component
public class TransaccionServiceConsumerImpl implements TransaccionServiceConsumer {

    @Autowired
    private MessageService service;

    @Autowired
    private TransaccionRepository repository;

    @Autowired
    private TransaccionEventAdapterImpl adapter;

    /**
     * Maneja los eventos recibidos desde Kafka. Determina si el evento es de tipo {@link TransaccionEvent}
     * o {@link EntityDeleteEventLong} y procesa cada uno en consecuencia.
     *
     * @param baseEvent El evento recibido.
     * @param messageId Identificador único del mensaje.
     * @param messageKey Clave del mensaje recibido.
     */
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${transacciones.topic}'}")
    @Override
    public void handleEvent(@Payload BaseEvent baseEvent,
                            @Header(value = "messageId", required = true)String messageId,
                            @Header(KafkaHeaders.RECEIVED_KEY)String messageKey) {
        if(baseEvent instanceof TransaccionEvent) {
            handleCreateEvent((TransaccionEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }
    }

    /**
     * Maneja la creación o modificación de una transacción.
     * Si la transacción no existe, se crea una nueva, luego se guarda en la base de datos.
     *
     * @param event El evento de transacción.
     * @param messageId Identificador único del mensaje.
     * @param messageKey Clave del mensaje recibido.
     */
    @Override
    @Transactional("transactionManager")
    public void handleCreateEvent(TransaccionEvent event, String messageId, String messageKey) {
        if(service.existeMessage(messageId)){
            return;
        }
        Transaccion transaccion = repository.findById(event.getId()).orElse(null);
        try{
            if(transaccion == null) {
                transaccion = new Transaccion();
            }
            transaccion = adapter.creacion(transaccion, event);
            repository.save(transaccion);
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, transaccion.getId().toString());
        } catch (Exception ex){
            throw new NotRetryableException(ex);
        }
    }

    /**
     * Maneja la eliminación de una transacción. Si la transacción existe, se elimina de la base de datos.
     *
     * @param eventDelete El evento de eliminación de transacción.
     * @param messageId Identificador único del mensaje.
     * @param messageKey Clave del mensaje recibido.
     */
    @Override
    @Transactional("transactionManager")
    public void handleDeleteEvent(EntityDeleteEventLong eventDelete, String messageId, String messageKey) {
        if(service.existeMessage(messageId)) {
            return;
        }
        Transaccion transaccion = repository.findById(eventDelete.getId()).orElse(null);
        try {
            if(transaccion == null){
                return;
            }
            repository.deleteById(eventDelete.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, transaccion.getId().toString());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }
}
