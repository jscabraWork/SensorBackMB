CREATE OR REPLACE VIEW grafica_dona AS
WITH orden_tickets_agregado AS (
    -- Agregamos tickets por orden para evitar multiplicación
    SELECT 
        ot.orden_id,
        COUNT(DISTINCT ot.ticket_id) as total_tickets,
        SUM(tar.precio + tar.servicio + tar.iva) as total_recaudado_tickets
    FROM orden_tickets ot
    JOIN tickets tk ON ot.ticket_id = tk.id
    JOIN tarifas tar ON tk.tarifa_id = tar.id
    WHERE tk.estado = 1
    GROUP BY ot.orden_id
),
transacciones_agregado AS (
    -- Agregamos transacciones por orden para evitar duplicación
    SELECT 
        t.orden_id,
        COUNT(DISTINCT t.id) as total_transacciones,
        SUM(t.amount) as total_amount_transacciones,
        MAX(t.metodo) as metodo -- Asumimos que una orden tiene un solo método
    FROM transacciones t
    WHERE t.status = 34
    GROUP BY t.orden_id
)
SELECT 
    o.evento_id,
    DATE(o.creation_date) as fecha,
    YEAR(o.creation_date) as anio,
    MONTH(o.creation_date) as mes,
    DAY(o.creation_date) as dia,
    CASE 
        WHEN op.id IS NOT NULL THEN 'punto_fisico'
        WHEN ta.metodo = 1 THEN 'tarjeta'
        WHEN ta.metodo = 2 THEN 'pse'
        ELSE 'otro'
    END as metodo_pago,
    COALESCE(ta.total_transacciones, 1) as total_transacciones,
    ota.total_tickets,
    ota.total_recaudado_tickets as total_recaudado,
    COALESCE(ta.total_amount_transacciones, ota.total_recaudado_tickets) as total_recaudado_transacciones
FROM ordenes o
LEFT JOIN transacciones_agregado ta ON o.id = ta.orden_id
LEFT JOIN ordenes_puntosfisicos op ON o.id = op.id
JOIN orden_tickets_agregado ota ON o.id = ota.orden_id
WHERE o.estado = 1 
  AND o.tipo != 5
  AND (ta.orden_id IS NOT NULL OR op.id IS NOT NULL); 