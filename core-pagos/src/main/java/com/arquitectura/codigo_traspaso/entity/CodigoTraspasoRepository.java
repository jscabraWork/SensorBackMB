package com.arquitectura.codigo_traspaso.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodigoTraspasoRepository extends JpaRepository<CodigoTraspaso, Long> {

    public CodigoTraspaso findByCodigo(String pIdBusqueda);

    public List<CodigoTraspaso> findByTicketId(Long pTicketId);
}
