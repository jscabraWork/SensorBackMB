package com.arquitectura.ticket_vendedores.ticket_puntofisico.service;

import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TicketPuntoFisicoEvent;
import com.arquitectura.ticket.entity.Ticket;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Uuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class TicketPuntoFisicoServiceImpl implements TicketPuntoFisicoService{

    @Value("${tickets-puntosF.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional("transactionManager")
    public void publicar(Ticket ticket, String puntoFisicoId) {

        TicketPuntoFisicoEvent ticketEvent = new TicketPuntoFisicoEvent(
                ticket.getId(),
                ticket.getNumero(),
                ticket.getEstado(),
                ticket.getTipo(),
                ticket.getLocalidad() != null ? ticket.getLocalidad().getId() : null,
                ticket.getTarifa() != null ? ticket.getTarifa().getId() : null,
                null,
                null,
                ticket.getPalco() != null ? ticket.getPalco().getId() : null,
                puntoFisicoId
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, "Ticket-puntofisico" + ticketEvent.getId(), ticketEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void publicarTicketsPuntofisico(List<Ticket> tickets, String puntoFisicoId) {
        tickets.forEach(ticket->{
            publicar(ticket, puntoFisicoId);
        });
    }

    @Override
    public void deleteTicketsPuntoFisicoById(List<Ticket> tickets) {
        tickets.forEach(ticket->{
            delete(ticket);
        });
    }


    @Transactional("transactionManager")
    @Override
    public void delete(Ticket ticket) {
        EntityDeleteEventLong ticketeDelete = new EntityDeleteEventLong(ticket.getId());
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, "Ticket-" + ticket.getId(),
                    ticketeDelete);
            record.headers().add("messageId", Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}
