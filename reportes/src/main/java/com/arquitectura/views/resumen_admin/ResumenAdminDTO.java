package com.arquitectura.views.resumen_admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigInteger;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ResumenAdminDTO {
    // Campos BIGINT -> BigInteger (SUM de BIGINT devuelve BigInteger)
    private BigInteger total_asistentes;
    private BigInteger total_ingresos;
    private BigInteger total_cortesias;
    private BigInteger total_asistentes_taquilla;
    private BigInteger total_pagos_taquilla;
    private BigInteger total_pagos_pse;
    private BigInteger total_pagos_tc;
    private BigInteger total_transacciones;
    private BigInteger total_compradores;
    private BigInteger total_asistentes_pse;
    private BigInteger total_asistentes_tc;
    private BigInteger total_tickets_vendidos_hoy;

    // Campos DOUBLE -> Double (SUM de DOUBLE devuelve Double)
    private Double total_recaudado;
    private Double total_recaudado_transacciones;
    private Double total_servicio_recaudado;
    private Double total_precio_recaudado;
    private Double total_iva_recaudado;
    private Double total_recaudado_hoy;
    private Double total_retefuente;
    private Double total_reteica;
    private Double total_parafiscal;
    private Double total_comision_alltickets;
    private Double total_comision_pasarela;
    private Double total_comision_3ds;
}