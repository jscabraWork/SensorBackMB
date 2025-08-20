use db_microservicio_reporte_sensor;

CREATE OR REPLACE VIEW historial AS
SELECT
    o.id as orden_id,
    tr.id as transaccion_id,
    o.evento_id as evento_id,
    tar.nombre as tarifa,
    d.nombre as dia,
    l.nombre as localidad,
    o.creation_date as fecha,
    o.valor_orden as valorOrden,
    tr.amount as monto,
    o.tipo as tipo,
    CASE
        WHEN o.tipo = 1 THEN 'Compra Estandar'
        WHEN o.tipo = 2 THEN 'Adiciones'
        WHEN o.tipo = 3 THEN 'Creación Alcancia'
        WHEN o.tipo = 4 THEN 'Aporte a Alcancia'
        WHEN o.tipo = 5 THEN 'Traspaso'
        WHEN o.tipo = 6 THEN 'Cortesía'
        ELSE 'TIPO DESCONOCIDO'
    END as tipo_nombre,
    tr.metodo_nombre as metodo,
    c.correo as correo,
    c.nombre as nombre,
    c.celular as telefono,
    c.numero_documento as documento,
    o.estado as estado,
    tr.status as status,
    p.nombre as promotor,
    p.numero_documento as promotor_numero_documento,
    a.id as alcancia_id,
    a.precio_parcial_pagado,
    a.precio_total,
    o.tarifa_id as tarifa_id,
    l.id as localidad_id,
    d.id as dia_id,
    COALESCE(COUNT(t.id), 0) as cantidad
FROM ordenes o
LEFT JOIN transacciones tr ON tr.orden_id = o.id
LEFT JOIN orden_tickets ot ON ot.orden_id = o.id
LEFT JOIN tickets t ON ot.ticket_id = t.id
JOIN tarifas tar ON o.tarifa_id = tar.id
JOIN localidades l ON tar.localidad_id = l.id
JOIN dias_localidades dl ON dl.localidad_id = l.id
JOIN dias d ON dl.dia_id = d.id
JOIN clientes c ON o.cliente_id = c.numero_documento
LEFT JOIN ordenes_promotor op ON o.id = op.id
LEFT JOIN promotores p ON op.promotor_numero_documento = p.numero_documento
LEFT JOIN ordenes_alcancia oa ON o.id = oa.id
LEFT JOIN alcancias a ON oa.alcancia_id = a.id
GROUP BY
    o.id, tr.id, o.evento_id, tar.nombre, d.nombre, l.nombre, o.creation_date,
    tr.amount, o.tipo, tr.metodo_nombre, c.correo, c.nombre, c.celular,
    c.numero_documento, o.estado, tr.status, p.nombre, p.numero_documento,
    o.tarifa_id, l.id, d.id, a.id, a.precio_parcial_pagado, a.precio_total;
