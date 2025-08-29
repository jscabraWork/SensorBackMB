package com.arquitectura.views.detalle_evento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;


import java.util.List;

@Repository
public interface DetalleEventoViewRepository extends JpaRepository<DetalleEventoView, Long> {

    List<DetalleEventoView> findByEventoIdOrderByTarifa(Long eventoId);

    @Query("SELECT d FROM DetalleEventoView d WHERE d.eventoId = :eventoId " +
            "AND (:tarifaId IS NULL OR d.tarifaId = :tarifaId) " +
            "AND (:localidadId IS NULL OR d.localidadId = :localidadId) " +
            "AND (:diaId IS NULL OR d.diaId = :diaId) " +
            "ORDER BY d.tarifa")
    List<DetalleEventoView> findDetalleFiltrado(
            @Param("eventoId") Long eventoId,
            @Param("tarifaId") Long tarifaId,
            @Param("localidadId") Long localidadId,
            @Param("diaId") Long diaId
    );

}
