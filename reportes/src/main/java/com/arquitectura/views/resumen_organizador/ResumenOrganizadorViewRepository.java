package com.arquitectura.views.resumen_organizador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ResumenOrganizadorViewRepository extends JpaRepository<ResumenOrganizadorView, Long> {

    @Query(value = """
        SELECT organizador_numero_documento,
            organizador,
            COUNT(*) as total_eventos,
            COALESCE(SUM(total_recaudado), 0.0) as dinero_total_recaudado,
            COALESCE(SUM(total_recaudado_transacciones), 0.0) as total_recaudado_transacciones,
            COALESCE(SUM(asistentes), 0) as total_asistentes,
            COALESCE(ROUND(AVG(total_recaudado), 2), 0.0) as promedio_dinero_recaudado,
            COALESCE(ROUND(AVG(asistentes), 2), 0.0) as promedio_asistentes,
            COALESCE(MAX(total_recaudado), 0.0) as maximo_recaudado_evento,
            COALESCE(MIN(total_recaudado), 0.0) as minimo_recaudado_evento,
            COALESCE(MAX(asistentes), 0) as maximo_asistentes_evento,
            COALESCE(MIN(asistentes), 0) as minimo_asistentes_evento,
            COALESCE(SUM(total_transacciones), 0) as total_transacciones_procesadas,
            COALESCE(SUM(total_compradores), 0) as total_compradores_unicos,
            COALESCE(ROUND(AVG(total_compradores), 2), 0.0) as promedio_compradores_por_evento,
            COALESCE(ROUND((SUM(asistentes) / NULLIF(SUM(total_compradores), 0)), 2), 0.0) as tickets_promedio_por_comprador,
            COALESCE(SUM(total_cortesias), 0) as total_cortesias
        FROM resumen_evento_organizador
        WHERE organizador_numero_documento = :numeroDocumento
            AND (:fechaInicio IS NULL OR :fechaFin IS NULL OR fecha_evento BETWEEN :fechaInicio AND :fechaFin)
        GROUP BY organizador_numero_documento, organizador
        """, nativeQuery = true)
    Object[] getResumenOrganizador(
            @Param("numeroDocumento") String numeroDocumento,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

}
