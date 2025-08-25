USE db_microservicio_reporte_sensor;

CREATE OR REPLACE VIEW resumen_evento AS
WITH base_tickets AS (
    SELECT
        ev.id as evento_id,
        t.id as ticket_id,
        t.estado as ticket_estado,
        t.tarifa_id,
        o.creation_date,
        o.tipo as orden_tipo,
        o.estado as orden_estado,
        o.cliente_id,
        tr_transac.id as transaccion_id,
        tr_transac.status as transac_status,
        tr_transac.metodo,
        tr_transac.amount,
        i.utilizado as ingreso_utilizado,
        tar.precio,
        tar.servicio,
        tar.iva,
        uvt.valor as uvt_valor
    FROM eventos ev
    LEFT JOIN dias d ON d.evento_id = ev.id
    LEFT JOIN dias_localidades dl ON dl.dia_id = d.id
    LEFT JOIN localidades l ON l.id = dl.localidad_id
    LEFT JOIN tickets t ON t.localidad_id = l.id
    LEFT JOIN orden_tickets ot ON ot.ticket_id = t.id
    LEFT JOIN ordenes o ON ot.orden_id = o.id
    LEFT JOIN transacciones tr_transac ON tr_transac.orden_id = o.id
    LEFT JOIN ingresos i ON i.ticket_id = t.id
    LEFT JOIN tarifas tar ON tar.id = t.tarifa_id
    LEFT JOIN uvt ON uvt.ano = YEAR(o.creation_date)
),
comisiones_data AS (
    SELECT
        MAX(CASE WHEN concepto = 'comisionAT' THEN valor END) as comision_at,
        MAX(CASE WHEN concepto = 'pasarela' THEN valor END) as comision_pasarela_valor,
        MAX(CASE WHEN concepto = '3ds' THEN valor END) as comision_3ds_valor
    FROM comisiones
    WHERE concepto IN ('comisionAT', 'pasarela', '3ds')
)
SELECT
    e.id as evento_id,
    e.nombre as nombre,

    -- Métricas de asistentes
    COUNT(DISTINCT CASE WHEN bt.ticket_estado = 1 THEN bt.ticket_id END) as asistentes,
    COUNT(DISTINCT CASE WHEN bt.ingreso_utilizado = true THEN bt.transaccion_id END) as ingresos,

    -- Métricas financieras principales
    COALESCE(SUM(CASE WHEN bt.ticket_estado = 1 THEN bt.precio + bt.servicio + bt.iva END), 0) as total_recaudado,
    COALESCE(SUM(CASE WHEN bt.transac_status = 34 AND bt.orden_tipo != 5 THEN bt.amount END), 0) as total_recaudado_transacciones,
    COALESCE(SUM(CASE WHEN bt.ticket_estado = 1 THEN bt.servicio END), 0) as servicio_recaudado,
    COALESCE(SUM(CASE WHEN bt.ticket_estado = 1 THEN bt.precio END), 0) as precio_recaudado,
    COALESCE(SUM(CASE WHEN bt.ticket_estado = 1 THEN bt.iva END), 0) as iva_recaudado,

    -- Cortesías
    COUNT(DISTINCT CASE WHEN bt.ticket_estado = 1 AND bt.precio = 0 THEN bt.ticket_id END) as total_cortesias,

    -- Métricas por canal
    COUNT(DISTINCT CASE WHEN bt.orden_estado = 1 AND bt.ticket_estado = 1 AND bt.orden_tipo IS NULL THEN bt.ticket_id END) as asistentes_taquilla,
    COUNT(DISTINCT CASE WHEN bt.orden_estado = 1 AND bt.orden_tipo IS NULL THEN bt.ticket_id END) as pagos_taquilla,

    -- Métricas por método de pago
    COUNT(DISTINCT CASE WHEN bt.transac_status = 34 AND bt.metodo = 2 THEN bt.transaccion_id END) as pagos_pse,
    COUNT(DISTINCT CASE WHEN bt.transac_status = 34 AND bt.metodo = 1 THEN bt.transaccion_id END) as pagos_tc,
    COUNT(DISTINCT CASE WHEN bt.transac_status = 34 THEN bt.transaccion_id END) as total_transacciones,
    COUNT(DISTINCT CASE WHEN bt.transac_status = 34 THEN bt.cliente_id END) as total_compradores,

    COUNT(DISTINCT CASE WHEN bt.orden_estado = 1 AND bt.metodo = 2 THEN bt.ticket_id END) as asistentes_pse,
    COUNT(DISTINCT CASE WHEN bt.orden_estado = 1 AND bt.metodo = 1 THEN bt.ticket_id END) as asistentes_tc,

    -- Métricas del día
    COUNT(DISTINCT CASE
        WHEN bt.orden_estado = 1 AND bt.ticket_estado = 1 AND bt.orden_tipo != 5
        AND DATE(bt.creation_date) = CURDATE() THEN bt.ticket_id END) as tickets_vendidos_hoy,

    COALESCE(SUM(CASE
        WHEN bt.orden_estado = 1 AND bt.ticket_estado = 1 AND bt.orden_tipo != 5
        AND DATE(bt.creation_date) = CURDATE()
        THEN bt.precio + bt.servicio + bt.iva END), 0) as total_recaudado_hoy,

    -- Cálculos de impuestos y comisiones
    COALESCE(SUM(CASE
        WHEN bt.transac_status = 34 AND bt.metodo = 1 AND bt.orden_tipo != 5
        THEN bt.amount * 0.015 END), 0) as retefuente,

    COALESCE(SUM(CASE
        WHEN bt.transac_status = 34 AND bt.metodo = 1 AND bt.orden_tipo != 5
        THEN bt.amount * 0.00414 END), 0) as reteica,

    COALESCE(SUM(CASE
        WHEN bt.orden_estado = 1 AND bt.orden_tipo != 5
        AND (bt.precio + bt.servicio + bt.iva) >= bt.uvt_valor
        THEN (bt.precio + bt.servicio + bt.iva) * 0.1 END), 0) as parafiscal,

    COALESCE((SUM(
    CASE WHEN bt.ticket_estado = 1 THEN bt.precio + bt.servicio + bt.iva END) * cd.comision_at), 0) * 1.19
    as comision_alltickets,

    COALESCE((COUNT(DISTINCT CASE
        WHEN bt.transac_status = 34 AND bt.metodo IN (1,2) AND bt.orden_tipo != 5
        THEN bt.transaccion_id END) * cd.comision_pasarela_valor), 0) as comision_pasarela,

    COALESCE((COUNT(DISTINCT CASE
        WHEN bt.metodo = 1 AND bt.orden_tipo != 5
        THEN bt.transaccion_id END) * cd.comision_3ds_valor), 0) as comision_3ds

FROM eventos e
CROSS JOIN comisiones_data cd
LEFT JOIN base_tickets bt ON bt.evento_id = e.id
GROUP BY e.id, e.nombre, cd.comision_at, cd.comision_pasarela_valor, cd.comision_3ds_valor
ORDER BY e.id;