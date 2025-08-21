package com.arquitectura.ticket.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByLocalidad_Dias_Evento_IdAndPuntofisico_NumeroDocumentoAndEstado(
            Long eventoId,
            String puntoId,
            Integer estado
    );
}
