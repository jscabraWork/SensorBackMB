package com.arquitectura.views.resumen_admin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResumenAdminDTO {

    private Integer totalAsistentes;
    private Integer totalIngresos;
    private Integer totalCortesias;
    private Integer totalAsistentesTaquilla;
    private Integer totalPagosTaquilla;
    private Integer totalPagosPse;
    private Integer totalPagosTc;
    private Integer totalTransacciones;
    private Integer totalCompradores;
    private Integer totalAsistentesPse;
    private Integer totalAsistentesTc;
    private Integer totalTicketsVendidosHoy;
    private Double totalRecaudado;
    private Double totalRecaudadoTransacciones;
    private Double totalServicioRecaudado;
    private Double totalPrecioRecaudado;
    private Double totalIvaRecaudado;
    private Double totalRecaudadoHoy;
    private Double totalRetefuente;
    private Double totalReteica;
    private Double totalParafiscal;
    private Double totalComisionAlltickets;
    private Double totalComisionPasarela;
    private Double totalComision3ds;

    // Constructor personalizado que maneja conversión de tipos desde MySQL
    //La conversión de la query nativa me dio muchos problemas de casting, por lo que decidí manejar los tipos como Object y convertirlos manualmente
    //Intente de todas las formas posibles y no hubo caso, así que esto es lo que mejor me funcionó y tambien es más seguro
    //ATTE: ISAAC
    public ResumenAdminDTO(
            Object total_asistentes, Object total_ingresos, Object total_recaudado,
            Object total_recaudado_transacciones, Object total_servicio_recaudado, 
            Object total_precio_recaudado, Object total_iva_recaudado, Object total_cortesias,
            Object total_asistentes_taquilla, Object total_pagos_taquilla, Object total_pagos_pse,
            Object total_pagos_tc, Object total_transacciones, Object total_compradores,
            Object total_asistentes_pse, Object total_asistentes_tc, Object total_tickets_vendidos_hoy,
            Object total_recaudado_hoy, Object total_retefuente, Object total_reteica,
            Object total_parafiscal, Object total_comision_alltickets, Object total_comision_pasarela,
            Object total_comision_3ds
    ) {
        // Campos Integer (conteos)
        this.totalAsistentes = total_asistentes instanceof Number ? ((Number) total_asistentes).intValue() : 0;
        this.totalIngresos = total_ingresos instanceof Number ? ((Number) total_ingresos).intValue() : 0;
        this.totalCortesias = total_cortesias instanceof Number ? ((Number) total_cortesias).intValue() : 0;
        this.totalAsistentesTaquilla = total_asistentes_taquilla instanceof Number ? ((Number) total_asistentes_taquilla).intValue() : 0;
        this.totalPagosTaquilla = total_pagos_taquilla instanceof Number ? ((Number) total_pagos_taquilla).intValue() : 0;
        this.totalPagosPse = total_pagos_pse instanceof Number ? ((Number) total_pagos_pse).intValue() : 0;
        this.totalPagosTc = total_pagos_tc instanceof Number ? ((Number) total_pagos_tc).intValue() : 0;
        this.totalTransacciones = total_transacciones instanceof Number ? ((Number) total_transacciones).intValue() : 0;
        this.totalCompradores = total_compradores instanceof Number ? ((Number) total_compradores).intValue() : 0;
        this.totalAsistentesPse = total_asistentes_pse instanceof Number ? ((Number) total_asistentes_pse).intValue() : 0;
        this.totalAsistentesTc = total_asistentes_tc instanceof Number ? ((Number) total_asistentes_tc).intValue() : 0;
        this.totalTicketsVendidosHoy = total_tickets_vendidos_hoy instanceof Number ? ((Number) total_tickets_vendidos_hoy).intValue() : 0;
        
        // Campos Double (valores monetarios)
        this.totalRecaudado = total_recaudado instanceof Number ? ((Number) total_recaudado).doubleValue() : 0.0;
        this.totalRecaudadoTransacciones = total_recaudado_transacciones instanceof Number ? ((Number) total_recaudado_transacciones).doubleValue() : 0.0;
        this.totalServicioRecaudado = total_servicio_recaudado instanceof Number ? ((Number) total_servicio_recaudado).doubleValue() : 0.0;
        this.totalPrecioRecaudado = total_precio_recaudado instanceof Number ? ((Number) total_precio_recaudado).doubleValue() : 0.0;
        this.totalIvaRecaudado = total_iva_recaudado instanceof Number ? ((Number) total_iva_recaudado).doubleValue() : 0.0;
        this.totalRecaudadoHoy = total_recaudado_hoy instanceof Number ? ((Number) total_recaudado_hoy).doubleValue() : 0.0;
        this.totalRetefuente = total_retefuente instanceof Number ? ((Number) total_retefuente).doubleValue() : 0.0;
        this.totalReteica = total_reteica instanceof Number ? ((Number) total_reteica).doubleValue() : 0.0;
        this.totalParafiscal = total_parafiscal instanceof Number ? ((Number) total_parafiscal).doubleValue() : 0.0;
        this.totalComisionAlltickets = total_comision_alltickets instanceof Number ? ((Number) total_comision_alltickets).doubleValue() : 0.0;
        this.totalComisionPasarela = total_comision_pasarela instanceof Number ? ((Number) total_comision_pasarela).doubleValue() : 0.0;
        this.totalComision3ds = total_comision_3ds instanceof Number ? ((Number) total_comision_3ds).doubleValue() : 0.0;
    }
}