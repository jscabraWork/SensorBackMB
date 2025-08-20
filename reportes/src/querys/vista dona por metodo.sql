-- Vista para diagrama de dona - ventas por método de pago
CREATE VIEW grafica_dona AS
SELECT 
    o.evento_id,
    DATE(COALESCE(t.creation_date, o.creation_date)) as fecha,
    YEAR(COALESCE(t.creation_date, o.creation_date)) as anio,
    MONTH(COALESCE(t.creation_date, o.creation_date)) as mes,
    DAY(COALESCE(t.creation_date, o.creation_date)) as dia,
    CASE 
        WHEN EXISTS (SELECT 1 FROM ordenes_puntosfisicos op WHERE op.id = o.id) THEN 'punto_fisico'
        WHEN t.metodo = 1 THEN 'tarjeta'
        WHEN t.metodo = 2 THEN 'pse'
        ELSE 'otro'
    END as metodo_pago,
    COUNT(DISTINCT COALESCE(t.id, o.id)) as total_transacciones,
    COUNT(DISTINCT ot.ticket_id) as total_tickets,
    SUM(tar.precio + tar.servicio + tar.iva) as total_recaudado
FROM ordenes o
LEFT JOIN transacciones t ON o.id = t.orden_id AND t.status = 34
LEFT JOIN ordenes_puntosfisicos op ON o.id = op.id
JOIN orden_tickets ot ON o.id = ot.orden_id
JOIN tickets tk ON ot.ticket_id = tk.id
JOIN tarifas tar ON tk.tarifa_id = tar.id
WHERE o.estado = 1 
  AND o.tipo != 5
  AND tk.estado = 1
  AND (t.status = 34 OR op.id IS NOT NULL) -- Solo transacciones aprobadas o puntos físicos
GROUP BY 
    o.evento_id, 
    DATE(COALESCE(t.creation_date, o.creation_date)),
    YEAR(COALESCE(t.creation_date, o.creation_date)),
    MONTH(COALESCE(t.creation_date, o.creation_date)),
    DAY(COALESCE(t.creation_date, o.creation_date)),
    CASE 
        WHEN EXISTS (SELECT 1 FROM ordenes_puntosfisicos op WHERE op.id = o.id) THEN 'punto_fisico'
        WHEN t.metodo = 1 THEN 'tarjeta'
        WHEN t.metodo = 2 THEN 'pse'
        ELSE 'otro'
    END;