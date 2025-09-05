package com.arquitectura.orden_traspaso.service;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.events.OrdenTraspasoEvent;
import com.arquitectura.events.TransaccionEvent;
import com.arquitectura.orden_traspaso.entity.OrdenTraspaso;
import com.arquitectura.orden_traspaso.entity.OrdenTraspasoRepository;
import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import com.arquitectura.transaccion.entity.Transaccion;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Uuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OrdenTraspasoServiceImpl extends CommonServiceImpl<OrdenTraspaso, OrdenTraspasoRepository> implements OrdenTraspasoService {

    @Autowired
    private TicketService ticketService;

    @Value("${ordenes.traspaso.topic}")
    private String ordenesTraspasoTopic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional("transactionManager")
    @Override
    public OrdenTraspaso transferirTicket(Ticket ticket, Cliente cliente, Cliente receptor) throws Exception {

        // Asigna el nuevo cliente al ticket
        ticket.setCliente(receptor);

        List<Ticket> tickets = new ArrayList<>(List.of(ticket));

        OrdenTraspaso orden = new OrdenTraspaso(
                receptor,
                cliente,
                tickets
        );

        Transaccion transaccion = new Transaccion(
                orden.getValorOrden(),
                cliente.getCorreo(),
                cliente.getNombre(),
                cliente.getNumeroDocumento(),
                7,
                (cliente.getCelular() != null && !cliente.getCelular().isEmpty()) ? cliente.getCelular() : "No aplica",
                34,
                orden
        );

        orden.setTransacciones(new ArrayList<>(List.of(transaccion)));

        //Guarda, publica el ticket y manda el QR
        ticketService.enviar(tickets);

        return this.saveKafka(orden);
    }


    private OrdenTraspasoEvent crearOrdenTraspasoEvent(OrdenTraspaso orden, Transaccion transaccion){

        // Crea un evento de creación para ser enviado a Kafka.
        List<Long> ticketsIds = new ArrayList<>();
        orden.getTickets().forEach(t->ticketsIds.add(t.getId()));

        OrdenTraspasoEvent ordenTraspaso = new OrdenTraspasoEvent(
                orden.getId(),
                orden.getEstado(),
                orden.getTipo(),
                orden.getEvento().getId(),
                orden.getValorOrden(),
                0.0,
                ticketsIds,
                orden.getCliente().getNumeroDocumento(),
                orden.getTarifa().getId(),
                orden.getReceptor().getNumeroDocumento()
        );

        TransaccionEvent transaccionEvent = new TransaccionEvent(
                transaccion.getId(),
                transaccion.getAmount(),
                transaccion.getEmail(),
                transaccion.getFullName(),
                transaccion.getIdPasarela(),
                transaccion.getIdPersona(),
                transaccion.getIp(),
                transaccion.getMetodo(),
                transaccion.getMetodoNombre(),
                transaccion.getPhone(),
                transaccion.getStatus(),
                transaccion.getIdBasePasarela(),
                transaccion.getOrden() != null ? transaccion.getOrden().getId() : null
        );

        ordenTraspaso.setTransaccion(transaccionEvent);

        return ordenTraspaso;
    }


    @Transactional("transactionManager")
    public OrdenTraspaso saveKafka(OrdenTraspaso pOrden) {

        OrdenTraspaso ordenBd = save(pOrden);

        OrdenTraspasoEvent ordenEvent = crearOrdenTraspasoEvent(ordenBd, ordenBd.getTransacciones().get(0));

        ProducerRecord<String, Object> record = new ProducerRecord<>(ordenesTraspasoTopic,"OrdenTraspaso-"+ordenBd.getId(),ordenEvent);
        record.headers().add("messageId", Uuid.randomUuid().toString().getBytes());

        CompletableFuture<SendResult<String, Object>> future= kafkaTemplate.send(record);
        future.whenComplete((result, exception)->{
            if(exception!=null) {
            }
            else {

            }
        });
        return ordenBd;
    }



}
