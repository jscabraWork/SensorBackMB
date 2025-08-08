package com.arquitectura.ticket_vendedores.ticket_promotor.service;

import com.arquitectura.events.EntityDeleteEventLong;
import com.arquitectura.events.TicketPromotorEvent;
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
public class TicketPromotorServiceImpl implements TicketPromotorService{

    @Value("${tickets-promotor.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional("transactionManager")
    @Override
    public void publicarTicketsPromotor(List<Ticket> tickets, String promotorId) {
        tickets.forEach(ticket -> {
            publicar(ticket, promotorId);
        });
    }

    @Override
    @Transactional("transactionManager")
    public void publicar(Ticket ticket, String promotorId) {

        TicketPromotorEvent ticketEvent = new TicketPromotorEvent(
                ticket.getId(),
                ticket.getNumero(),
                ticket.getEstado(),
                ticket.getTipo(),
                ticket.getLocalidad() != null ? ticket.getLocalidad().getId() : null,
                ticket.getTarifa() != null ? ticket.getTarifa().getId() : null,
                null,
                null,
                ticket.getPalco() != null ? ticket.getPalco().getId() : null,
                promotorId
        );

        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic, "Ticket-Promotor" + ticketEvent.getId(), ticketEvent);
            record.headers().add("messageId", org.apache.kafka.common.Uuid.randomUuid().toString().getBytes());
            kafkaTemplate.send(record).get();
        } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }
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

    @Override
    public void deleteTicketsPromotorById(List<Ticket> tickets) {
        tickets.forEach(this::delete);
    }

}
