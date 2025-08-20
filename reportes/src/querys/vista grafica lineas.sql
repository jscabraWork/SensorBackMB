CREATE OR REPLACE VIEW grafica_lineas AS
WITH orden_agregado AS (
    -- Agregamos tickets por orden
    SELECT 
        o.id as orden_id,
        o.evento_id,
        DATE(o.creation_date) as fecha,
        YEAR(o.creation_date) as anio,
        MONTH(o.creation_date) as mes,
        DAY(o.creation_date) as dia,
        COUNT(DISTINCT CASE WHEN tk.estado = 1 THEN ot.ticket_id END) as total_tickets,
        SUM(CASE WHEN tk.estado = 1 THEN tar.precio + tar.servicio + tar.iva ELSE 0 END) as total_recaudado_tickets,
        SUM(CASE WHEN tk.estado = 1 THEN tar.precio ELSE 0 END) as total_precio,
        SUM(CASE WHEN tk.estado = 1 THEN tar.servicio ELSE 0 END) as total_servicio,
        SUM(CASE WHEN tk.estado = 1 THEN tar.iva ELSE 0 END) as total_iva
    FROM ordenes o
    JOIN orden_tickets ot ON o.id = ot.orden_id  
    JOIN tickets tk ON ot.ticket_id = tk.id
    JOIN tarifas tar ON tk.tarifa_id = tar.id
    WHERE o.estado = 1 AND o.tipo != 5
    GROUP BY o.id, o.evento_id, DATE(o.creation_date), YEAR(o.creation_date), MONTH(o.creation_date), DAY(o.creation_date)
),
transacciones_agregado AS (
    -- Agregamos transacciones por orden
    SELECT 
        t.orden_id,
        SUM(t.amount) as total_amount_transacciones
    FROM transacciones t
    WHERE t.status = 34
    GROUP BY t.orden_id
)
SELECT 
    oa.evento_id,
    oa.fecha,
    oa.anio,
    oa.mes,
    oa.dia,
    
    -- Contamos órdenes únicas por fecha
    COUNT(DISTINCT oa.orden_id) as total_ordenes,
    
    -- Sumamos tickets de todas las órdenes
    SUM(oa.total_tickets) as total_tickets,
    
    -- Sumamos recaudado de todas las órdenes
    SUM(oa.total_recaudado_tickets) as total_recaudado,
    SUM(oa.total_precio) as total_precio,
    SUM(oa.total_servicio) as total_servicio,
    SUM(oa.total_iva) as total_iva,
    
    -- Sumamos transacciones SIN multiplicar por tickets
    SUM(COALESCE(ta.total_amount_transacciones, 0)) as total_recaudado_transacciones

FROM orden_agregado oa
LEFT JOIN transacciones_agregado ta ON oa.orden_id = ta.orden_id
GROUP BY oa.evento_id, oa.fecha, oa.anio, oa.mes, oa.dia
ORDER BY oa.evento_id, oa.fecha;
