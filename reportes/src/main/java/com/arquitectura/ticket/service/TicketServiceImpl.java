package com.arquitectura.ticket.service;

import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.entity.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketServiceImpl extends CommonServiceImpl<Ticket, TicketRepository> implements TicketService {

    @Override
    public List<Ticket> saveAll(List<Ticket> tickets) {
        return repository.saveAll(tickets);
    }
    
}
