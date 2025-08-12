package com.arquitectura.ticket.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    List<Ticket> findByLocalidad_Dias_Evento_IdAndPromotor_NumeroDocumentoAndEstado(
            Long eventoId, 
            String promotorNumeroDocumento, 
            Integer estado
    );
}
