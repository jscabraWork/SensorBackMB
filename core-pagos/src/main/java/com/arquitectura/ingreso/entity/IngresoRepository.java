package com.arquitectura.ingreso.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IngresoRepository extends JpaRepository<Ingreso,Long> {

    @Query("SELECT i FROM Ingreso i " +
            "LEFT JOIN FETCH i.dia " +
            "WHERE i.ticket.id IN :ticketIds")
    List<Ingreso> findByTicketIdInWithDia(@Param("ticketIds") List<Long> ticketIds);

}
