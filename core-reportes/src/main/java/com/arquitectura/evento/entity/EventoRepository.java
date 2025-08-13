package com.arquitectura.evento.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Evento,Long> {

    List<Evento> findByOrganizadoresNumeroDocumentoAndEstadoNot(String numeroDocumento, Integer pEstado);

    List<Evento> findByOrganizadoresNumeroDocumentoAndEstado(String numeroDocumento, Integer pEstado);


}
