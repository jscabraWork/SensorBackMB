package com.arquitectura.orden_puntofisico.service;

import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.OrdenEvent;
import com.arquitectura.events.OrdenPuntoFisicoEvent;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.helper.OrdenCreationHelper;
import com.arquitectura.orden.service.OrdenService;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisico;
import com.arquitectura.orden_puntofisico.entity.OrdenPuntoFisicoRepository;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import com.arquitectura.ticket_vendedores.ticket_puntofisico.service.TicketPuntoFisicoService;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.service.TransaccionService;
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
public class OrdenPuntoFisicoServiceImpl extends CommonServiceImpl<OrdenPuntoFisico, OrdenPuntoFisicoRepository> implements OrdenPuntoFisicoService {

    private static final Logger logger = LoggerFactory.getLogger(OrdenPuntoFisicoServiceImpl.class);

    @Autowired
    private OrdenCreationHelper creationHelper;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TicketPuntoFisicoService ticektPuntoFisicoService;

    @Value("${ordenes.puntosF.topic}")
    private String ordenesTopic;

    @Autowired
    private OrdenService ordenService;

    @Transactional("transactionManager")
    @Override
    public OrdenPuntoFisico crearOrdenNoNumerada(Integer pCantidad, Long pEventoId, String pNumeroDocumento, Long pLocalidadId, String pPuntoFisicoId) throws Exception {

        // Factory para crear OrdenPuntoFisico
        OrdenCreationHelper.OrdenFactory<OrdenPuntoFisico> factory = (
                evento,
                cliente,
                tickets) -> new OrdenPuntoFisico(evento, cliente, tickets, pPuntoFisicoId);

        OrdenPuntoFisico orden = creationHelper.crearOrdenNoNumerada(pCantidad, pEventoId, pNumeroDocumento, pLocalidadId, factory);

        return saveKafka(orden);
    }

    @Transactional("transactionManager")
    @Override
    public OrdenPuntoFisico crearOrdenNumerada(List<Ticket> tickets, Long pEventoId, String pNumeroDocumento, String pPuntoFisicoId) throws Exception {

        OrdenCreationHelper.OrdenFactory<OrdenPuntoFisico> factory = (evento, cliente, ticketsList) -> new OrdenPuntoFisico(evento, cliente, ticketsList, pPuntoFisicoId);

        OrdenPuntoFisico orden = creationHelper.crearOrdenNumerada(
                tickets,
                pEventoId,
                pNumeroDocumento,
                factory);

        return saveKafka(orden);
    }

    @Transactional("transactionManager")
    @Override
    public OrdenPuntoFisico crearOrdenPalcoIndividual(Long pTicketPadreId, Integer pCantidad, Long pEventoId,
                                                      String pNumeroDocumento, String pPuntoFisicoId) throws Exception {

        OrdenCreationHelper.OrdenFactory<OrdenPuntoFisico> factory = (evento, cliente, ticketsList)
                -> new OrdenPuntoFisico(evento, cliente, ticketsList, pPuntoFisicoId);

        OrdenPuntoFisico orden = creationHelper.crearOrdenPalcoIndividual(
                pTicketPadreId,
                pCantidad,
                pEventoId,
                pNumeroDocumento,
                factory);

        return this.save(orden);
    }

    @Transactional("transactionManager")
    @Override
    public OrdenPuntoFisico crearOrdenNoNumeradaConTarifa(Integer pCantidad, Long pEventoId, String pNumeroDocumento,
                                                          Long pLocalidadId, Long pTarifaId, String pPuntoFisicoId) throws Exception {

        // Factory para crear OrdenPuntoFisico con tarifa específica
        OrdenCreationHelper.OrdenFactoryConTarifa<OrdenPuntoFisico> factory = (
                evento,
                cliente,
                tickets,
                tarifa) -> new OrdenPuntoFisico(evento, cliente, tickets, tarifa, pPuntoFisicoId);

        OrdenPuntoFisico orden = creationHelper.crearOrdenNoNumeradaConTarifa(pCantidad, pEventoId, pNumeroDocumento, pLocalidadId, pTarifaId, factory);

        return saveKafka(orden);
    }

    @Override
    public List<OrdenPuntoFisico> getAllOrdenesByClienteNumeroDocumento(String numeroDocumento) {
        return repository.findByClienteNumeroDocumento(numeroDocumento);
    }

    @Override
    public List<OrdenPuntoFisico> getAllOrdenesByPuntoFisicoNumeroDocumento(String puntoFisicoNumeroDocumento) {
        return repository.findByPuntoFisicoNumeroDocumento(puntoFisicoNumeroDocumento);
    }

    @Transactional("transactionManager")
    @Override
    public OrdenPuntoFisico saveKafka(OrdenPuntoFisico pOrden) {

        OrdenPuntoFisico orden = this.save(pOrden);
        OrdenPuntoFisicoEvent ordenEvent = new OrdenPuntoFisicoEvent(
                orden.getId(),
                orden.getEstado(),
                orden.getTipo(),
                orden.getEvento() != null ? orden.getEvento().getId() : null,
                orden.getValorOrden(),
                orden.getValorSeguro(),
                orden.getTickets() != null ? orden.getTickets().stream().map(Ticket::getId).collect(Collectors.toList()) : List.of(),
                orden.getCliente() != null ? orden.getCliente().getNumeroDocumento() : null,
                orden.getTarifa() != null ? orden.getTarifa().getId() : null,
                orden.getPuntoFisicoNumeroDocumento()
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ordenesTopic, "OrdenPuntoFisico-" + ordenEvent.getId(), ordenEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            logger.error("Error al enviar evento de orden punto físico a Kafka", e);
            e.printStackTrace();
        }
        return orden;
    }

    @Override
    public OrdenPuntoFisico confirmar(Long pOrdenId, Integer pMetodo) throws Exception {

        OrdenPuntoFisico orden = repository.findById(pOrdenId)
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna orden de punto físico con el id proporcionado"));

        Transaccion transaccionRepetida = transaccionService.getTransaccionRepetida(34, orden.getId());

        //Si ya existe una transacción repetida, no se crea una nueva
        if(transaccionRepetida!=null){
            return null;
        }

        transaccionService.crearTransaccionPuntoFisico(orden, pMetodo, 34);

        // Confirmar la orden, vende los tickets y les asigna el cliente y la tarifa
        orden.confirmar();

        //Persiste el ticket vendido, con el cliente y la tarifa, y envia los qrs
        ticketService.enviar(orden.getTickets());

        // Publica los tickets para el microservicio de puntosfísico
        ticektPuntoFisicoService.publicarTicketsPuntofisico(orden.getTickets(), orden.getPuntoFisicoNumeroDocumento());

        return saveKafka(orden);
    }

    @Override
    @Transactional("transactionManager")
    public void cancelar(Long pOrdenId) throws Exception {
        OrdenPuntoFisico orden = findById(pOrdenId);
        if(orden==null || orden.getEstado() !=3) {
            return;
        }
        ordenService.rechazar(orden);
        //Eliminar los tickets del microservicio de puntos físicos
        ticektPuntoFisicoService.deleteTicketsPuntoFisicoById(orden.getTickets());
    }

    @Transactional("transactionManager")
    @Override
    public void deleteById(Long pId) {
        OrdenPuntoFisico orden = repository.findById(pId)
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna orden de punto físico con el id proporcionado"));

        EntityDeleteEventLong deleteEvent = new EntityDeleteEventLong();
        deleteEvent.setId(pId);

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(ordenesTopic, "OrdenPuntoFisico-Delete-" + pId, deleteEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            logger.error("Error al enviar evento de eliminación de orden punto físico a Kafka", e);
            e.printStackTrace();
        }

        repository.deleteById(pId);
    }
}
