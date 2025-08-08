package com.arquitectura.ticket_vendedores.ticket_promotor.service;

import com.arquitectura.ticket.entity.Ticket;

import java.util.List;

public interface TicketPromotorService {

    public void publicar(Ticket ticket, String promotorId);

    public void publicarTicketsPromotor(List<Ticket> tickets, String promotorId);

    public void deleteTicketsPromotorById(List<Ticket> tickets);

    public void delete(Ticket ticket);
}
