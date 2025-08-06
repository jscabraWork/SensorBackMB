package com.arquitectura.evento.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {


    @Query("SELECT e FROM Evento e JOIN e.puntosFisicos p WHERE p.numeroDocumento = :puntofisicoId")
    List<Evento> findByPuntoFisicoId(@Param("puntofisicoId") String puntofisicoId);

    List<Evento> findByEstadoNot(int estado);

    List<Evento> findByEstado(int estado);

    List<Evento> findByPuntosFisicosNumeroDocumentoAndEstadoNot(String numeroDocumento, Integer estado);

}
