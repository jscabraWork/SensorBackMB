package com.arquitectura.orden_promotor.service;
import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenPromotorEvent;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.helper.OrdenCreationHelper;
import com.arquitectura.orden_promotor.entity.OrdenPromotor;
import com.arquitectura.orden_promotor.entity.OrdenPromotorRepository;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import com.arquitectura.ticket_vendedores.ticket_promotor.service.TicketPromotorService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrdenPromotorServiceImpl extends CommonServiceImpl<OrdenPromotor, OrdenPromotorRepository> implements OrdenPromotorService {

    private static final Logger logger = LoggerFactory.getLogger(OrdenPromotorServiceImpl.class);

    @Autowired
    private OrdenCreationHelper creationHelper;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketPromotorService ticketPromotorService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${ordenes.promotores.topic}")
    private String ordenesTopic;

    @Transactional("transactionManager")
    @Override
    public OrdenPromotor crearOrdenNoNumerada(Integer pCantidad, Long pEventoId, String pNumeroDocumento,
                                              Long pLocalidadId, String pPromotorId) throws Exception {

        // Factory para crear OrdenPromotor
        OrdenCreationHelper.OrdenFactory<OrdenPromotor> factory = (evento, cliente, tickets) ->
                new OrdenPromotor(evento,cliente,tickets,pPromotorId);

        OrdenPromotor orden = creationHelper.crearOrdenNoNumerada(pCantidad, pEventoId, pNumeroDocumento, pLocalidadId, factory);

        //publicar estado en proceso de los tickets
        ticketService.saveAllKafka(orden.getTickets());

        return saveKafka(orden);
    }

    @Transactional("transactionManager")
    @Override
    public OrdenPromotor crearOrdenNumerada(List<Ticket> tickets, Long pEventoId, String pNumeroDocumento,
                                            String pPromotorId) throws Exception {

        OrdenCreationHelper.OrdenFactory<OrdenPromotor> factory =(evento, cliente, ticketsList) ->
                new OrdenPromotor(evento, cliente, ticketsList, pPromotorId);

        OrdenPromotor orden = creationHelper.crearOrdenNumerada(
                tickets,
                pEventoId,
                pNumeroDocumento,
                factory);

        ticketService.saveAllKafka(orden.getTickets());
        return saveKafka(orden);
    }

    @Transactional("transactionManager")
    @Override
    public OrdenPromotor crearOrdenPalcoIndividual(Long pTicketPadreId, Integer pCantidad, Long pEventoId,
                                                   String pNumeroDocumento, String pPromotorId) throws Exception {

        OrdenCreationHelper.OrdenFactory<OrdenPromotor> factory = (evento, cliente, ticketsList) ->
                new OrdenPromotor(evento, cliente, ticketsList, pPromotorId);

        OrdenPromotor orden = creationHelper.crearOrdenPalcoIndividual(
                pTicketPadreId,
                pCantidad,
                pEventoId,
                pNumeroDocumento,
                factory);

        ticketService.saveAllKafka(orden.getTickets());
        return this.save(orden);
    }

    @Override
    public List<OrdenPromotor> getAllOrdenesByClienteNumeroDocumento(String numeroDocumento) {
        return repository.findByClienteNumeroDocumento(numeroDocumento);
    }

    @Override
    public List<OrdenPromotor> getAllOrdenesByPromotorNumeroDocumento(String promotorNumeroDocumento) {
        return repository.findByPromotorNumeroDocumento(promotorNumeroDocumento);
    }

    @Transactional("transactionManager")
    @Override
    public OrdenPromotor saveKafka(OrdenPromotor pOrden) {
        OrdenPromotor orden = this.save(pOrden);

        OrdenPromotorEvent ordenEvent = new OrdenPromotorEvent(
                orden.getId(),
                orden.getEstado(),
                orden.getTipo(),
                orden.getEvento() != null ? orden.getEvento().getId() : null,
                orden.getValorOrden(),
                orden.getValorSeguro(),
                orden.getTickets() != null ? orden.getTickets().stream().map(Ticket::getId).collect(Collectors.toList()) : List.of(),
                orden.getCliente() != null ? orden.getCliente().getNumeroDocumento() : null,
                orden.getTarifa() != null ? orden.getTarifa().getId() : null,
                orden.getPromotorNumeroDocumento()
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ordenesTopic, "OrdenPromotor-" + ordenEvent.getId(), ordenEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            logger.error("Error al enviar evento de orden promotor a Kafka", e);
            e.printStackTrace();
        }
        return orden;
    }

    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        OrdenPromotor orden = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna orden de promotor con el id proporcionado"));

        EntityDeleteEventLong deleteEvent = new EntityDeleteEventLong();
        deleteEvent.setId(pId);

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ordenesTopic, "OrdenPromotor-Delete-" + pId, deleteEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            logger.error("Error al enviar evento de eliminación de orden promotor a Kafka", e);
            e.printStackTrace();
        }

        repository.deleteById(pId);
    }

    //Se utiliza para publicar evento de venta de tickets de orden promotor
    @Override
    @Transactional("transactionManager")
    public void publicarVentaPromotor(Orden orden){

        OrdenPromotor ordenPromotor = findById(orden.getId());

         //Si la orden promotor es null, no se hace nada
        if(ordenPromotor == null) {
            return;
       }
        //Publicar los tickets al microservicio de promotores
         ticketPromotorService.publicarTicketsPromotor(orden.getTickets(), ordenPromotor.getPromotorNumeroDocumento());
    }
}
