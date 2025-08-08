package com.arquitectura.ticket_vendedores.ticket_puntofisico.service;

import com.arquitectura.ticket.entity.Ticket;

import java.util.List;

public interface TicketPuntoFisicoService {

    public void publicar(Ticket ticket, String puntoFisicoId);

    public void publicarTicketsPuntofisico(List<Ticket> tickets, String puntoFisicoId);

    public void deleteTicketsPuntoFisicoById(List<Ticket> tickets);

    public void delete(Ticket ticket);
}
