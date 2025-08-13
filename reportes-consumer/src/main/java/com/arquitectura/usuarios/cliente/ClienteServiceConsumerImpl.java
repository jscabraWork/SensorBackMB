package com.arquitectura.usuarios.cliente;


import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.cliente.entity.ClienteRepository;
import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.EntityDeleteEventString;
import com.arquitectura.events.UsuarioEvent;
import com.arquitectura.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * Componente que consume eventos relacionados con la entidad Cliente desde Kafka.
 * Escucha los mensajes en los temas especificados y maneja eventos de creación/modificación y eliminación.
 */
@Component
public class ClienteServiceConsumerImpl implements ClienteServiceConsumer{

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private MessageService service;

    @Autowired
    private ClienteEventAdapter adapter;

    @Override
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${clientes.topic}'}")
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

    /**
     * Maneja los eventos de creación/modificación de un cliente.
     *
     * @param event El evento que contiene los datos de la ciudad a crear o modificar.
     * @param messageId El ID del mensaje recibido desde Kafka.
     * @param messageKey La clave del mensaje recibido desde Kafka.
     * @throws NotRetryableException Si ocurre un error al procesar el evento.
     */
    @Override
    public void handleCreateEvent(UsuarioEvent event, String messageId, String messageKey) {
        if (service.existeMessage(messageId)) {
            return;
        }
        Cliente cliente = repository.findById(event.getId()).orElse(null);
        try {
            if (cliente == null) {
                cliente = new Cliente();
            }
            cliente = adapter.creacion(cliente, event);
            repository.save(cliente);
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
        try {
            service.crearMensaje(messageId, event.getId());
        } catch (Exception ex) {
            throw new NotRetryableException(ex);
        }
    }

    //Los clientes nunca se eliminan, por lo que este método no se implementa.
    @Override
    public void handleDeleteEvent(EntityDeleteEventString eventDelete, String messageId, String messageKey) {

    }

}
