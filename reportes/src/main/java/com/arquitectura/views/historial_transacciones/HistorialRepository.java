package com.arquitectura.views.historial_transacciones;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para consultar el historial de transacciones
 * Permite filtrar por evento y status de transacción con paginación
 */
@Repository
public interface HistorialRepository extends JpaRepository<HistorialView, Long> {

    /**
     * Buscar transacciones por evento ID y status con paginación
     * Ordenado por fecha descendente
     */
    @Query("SELECT h FROM HistorialView h WHERE h.eventoId = :eventoId AND h.status = :status ORDER BY h.fecha DESC")
    Page<HistorialView> findByEventoIdAndStatusOrderByFechaDesc(
            @Param("eventoId") Long eventoId,
            @Param("status") Integer status,
            Pageable pageable
    );

    /**
     * Buscar transacciones por múltiples filtros opcionales con rango de fechas
     */
    @Query("""
       SELECT h FROM HistorialView h WHERE 
       (:eventoId IS NULL OR h.eventoId = :eventoId) AND 
       (:status IS NULL OR :status = 35 OR h.status = :status) AND 
       (:status != 35 OR h.estado = 3) AND 
       (:tipo IS NULL OR h.tipo = :tipo) AND 
       (:fechaInicio IS NULL OR h.fecha >= :fechaInicio) AND 
       (:fechaFin IS NULL OR h.fecha <= :fechaFin) 
       ORDER BY h.fecha DESC
       """)
    Page<HistorialView> findByFiltrosOrderByFechaDesc(
            @Param("eventoId") Long eventoId,
            @Param("status") Integer status,
            @Param("tipo") Integer tipo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );


    List<HistorialView> findByEventoIdAndStatusOrderByFechaDesc(Long eventoId, Integer status);

}
