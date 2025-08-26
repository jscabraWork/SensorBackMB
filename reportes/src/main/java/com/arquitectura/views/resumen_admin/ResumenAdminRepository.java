package com.arquitectura.views.resumen_admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumenAdminRepository extends JpaRepository<ResumenAdmin, Long> {

    @Query(value = """
    SELECT SUM(asistentes) as total_asistentes,
        SUM(ingresos) as total_ingresos,
        SUM(total_recaudado) as total_recaudado,
        SUM(total_recaudado_transacciones) as total_recaudado_transacciones,
        SUM(servicio_recaudado) as total_servicio_recaudado,
        SUM(precio_recaudado) as total_precio_recaudado,
        SUM(iva_recaudado) as total_iva_recaudado,
        SUM(total_cortesias) as total_cortesias,
        SUM(asistentes_taquilla) as total_asistentes_taquilla,
        SUM(pagos_taquilla) as total_pagos_taquilla,
        SUM(pagos_pse) as total_pagos_pse,
        SUM(pagos_tc) as total_pagos_tc,
        SUM(total_transacciones) as total_transacciones,
        SUM(total_compradores) as total_compradores,
        SUM(asistentes_pse) as total_asistentes_pse,
        SUM(asistentes_tc) as total_asistentes_tc,
        SUM(tickets_vendidos_hoy) as total_tickets_vendidos_hoy,
        SUM(total_recaudado_hoy) as total_recaudado_hoy,
        SUM(retefuente) as total_retefuente,
        SUM(reteica) as total_reteica,
        SUM(parafiscal) as total_parafiscal,
        SUM(comision_alltickets) as total_comision_alltickets,
        SUM(comision_pasarela) as total_comision_pasarela,
        SUM(comision_3ds) as total_comision_3ds
    FROM resumen_admin
    WHERE 
        (:eventoId IS NULL OR evento_id = :eventoId)
        AND (:anio IS NULL OR anio = :anio)
        AND (:mes IS NULL OR mes = :mes)
        AND (:dia IS NULL OR dia = :dia)
    """, nativeQuery = true)
    public ResumenAdminDTO getResumenAdminDTO(
            @Param("eventoId") Long eventoId,
            @Param("anio") Integer anio,
            @Param("mes") Integer mes,
            @Param("dia") Integer dia
    );


}
