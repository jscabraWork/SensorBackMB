package com.arquitectura.orden.orden_alcancia;

import com.arquitectura.alcancia.consumer.AlcanciaConsumerService;
import com.arquitectura.alcancia.entity.AlcanciaRepository;
import com.arquitectura.error.NotRetryableException;
import com.arquitectura.events.AlcanciaEvent;
import com.arquitectura.events.BaseEvent;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenAlcanciaEvent;
import com.arquitectura.message.service.MessageService;
import com.arquitectura.orden.entity.OrdenRepository;
import com.arquitectura.orden_alcancia.entity.OrdenAlcancia;
import com.arquitectura.orden_alcancia.entity.OrdenAlcanciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrdenConsumerAlcanciaServiceImpl implements OrdenConsumerAlcancialService {

    @Autowired
    private OrdenAlcanciaRepository repository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private MessageService service;

    @Autowired
    private OrdenAlcanciaEventAdapter adapter;

    @Autowired
    private AlcanciaConsumerService alcanciaService;

    @Autowired
    private AlcanciaRepository alcanciaRepository;


    @Override
    @Transactional("transactionManager")
    @KafkaListener(topics = "#{'${ordenes.alcancias.topic}'}")
    public void handleEvent(@Payload BaseEvent baseEvent,
                            @Header(value = "messageId", required = true) String messageId,
                            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey){

        if (baseEvent instanceof OrdenAlcanciaEvent) {
            handleCreateEvent((OrdenAlcanciaEvent) baseEvent, messageId, messageKey);
        } else if (baseEvent instanceof EntityDeleteEventLong) {
            handleDeleteEvent((EntityDeleteEventLong) baseEvent, messageId, messageKey);
        } else {
            throw new NotRetryableException("Unsupported event type: " + baseEvent.getClass().getName());
        }

    }

    @Transactional("transactionManager")
    @Override
    public void handleCreateEvent(OrdenAlcanciaEvent event, String messageId, String messageKey) {

        if (service.existeMessage(messageId)) {
            return;
        }
        System.out.println("EVENTO RECIBIDO: " + event);

        OrdenAlcancia orden = repository.findById(event.getId()).orElse(null);
        try {
            if (orden == null) {
                // Verificar si existe una orden base en la tabla 'ordenes'
                boolean existeOrdenBase = ordenRepository.existsById(event.getId());
                
                if (existeOrdenBase) {

                    //Si es una orden de creacion de alcancía, se debe crear la alcancia antes
                    if(event.getAlcanciaEvent()!=null){
                        AlcanciaEvent alcanciaEvent = event.getAlcanciaEvent();

                        if(!alcanciaRepository.existsById(alcanciaEvent.getId())){
                            alcanciaService.handleCreateEvent(alcanciaEvent,
                                    messageId+alcanciaEvent.getId().toString(),
                                    alcanciaEvent.getId().toString());
                        }
                    }

                    // ESCENARIO A: La orden base ya existe → Solo insertar en ordenes_alcancia
                    repository.insertOrdenAlcancia(event.getId(), event.getAlcanciaId());
                    
                    // Recuperar la OrdenAlcancia recién creada
                    orden = repository.findById(event.getId())
                            .orElseThrow(() -> new RuntimeException("Error al crear la orden alcancía con ID: " + event.getId()));
                } else {
                    // ESCENARIO B: No existe orden base → Crear orden completa desde cero
                    orden = new OrdenAlcancia();
                }
            }

            orden = adapter.creacion(orden, event);

            // Save: actualizar si ya existía, crear si es nueva
            repository.save(orden);
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
        OrdenAlcancia orden = repository.findById(eventDelete.getId()).orElse(null);
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
