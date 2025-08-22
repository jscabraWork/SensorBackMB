package com.arquitectura.alcancia.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlcanciaRepository extends JpaRepository<Alcancia,Long> {

    @Query("SELECT DISTINCT a FROM Alcancia a " +
           "JOIN a.tickets t " +
           "JOIN t.localidad l " +
           "JOIN l.dias d " +
           "WHERE d.evento.id = :eventoId " +
           "AND (:estado IS NULL OR a.estado = :estado) " +
           "ORDER BY a.creationDate DESC")
    List<Alcancia> findByEventoIdAndEstado(
        @Param("eventoId") Long eventoId,
        @Param("estado") Integer estado
    );

}
