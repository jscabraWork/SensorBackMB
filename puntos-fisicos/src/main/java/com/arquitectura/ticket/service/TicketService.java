package com.arquitectura.ticket.service;

import com.arquitectura.services.CommonService;
import com.arquitectura.ticket.entity.Ticket;

import java.util.List;

public interface TicketService extends CommonService<Ticket> {
    
    List<Ticket> findByEventoIdAndPuntoNumeroDocumentoAndEstado(
            Long eventoId, 
            String puntoId,
            Integer estado
    );

}
