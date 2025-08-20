package com.arquitectura.ticket.entity;

import org.springframework.data.domain.Page;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Trae todos los tickets por su orden id
     * @param ordenId El ID de la orden
     * @return ResponseEntity con el código de estado 204 (No Content)
     */
    List<Ticket> findByOrdenesId(Long ordenId);
}
