package com.arquitectura.tarifa.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TarifaRepository extends JpaRepository<Tarifa,Long> {

    List<Tarifa> findAllByEstadoAndLocalidadId(int estado, Long localidadId);

    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.tarifa.id = :tarifaId")
    boolean existsTicketsByTarifaId(@Param("tarifaId") Long tarifaId);

    List<Tarifa> findAllByLocalidadId(Long localidadId);

    @Query(value = "SELECT DISTINCT t.* FROM tarifas t " +
            "JOIN localidades l ON t.localidad_id = l.id " +       // Relación directa de tarifa a localidad
            "JOIN dias_localidades dl ON l.id = dl.localidades_id " + // Tabla de unión entre días y localidades
            "JOIN dias d ON dl.dias_id = d.id " +                  // Tabla de días
            "JOIN eventos e ON d.evento_id = e.id " +              // Relación con eventos
            "WHERE e.id = :eventoId", nativeQuery = true)
    List<Tarifa> findAllByEventoId(@Param("eventoId") Long eventoId);
}

