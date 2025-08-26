SELECT
    l.nombre as nombre_localidad,
    l.id as localidad_id,
    e.nombre as nombre_evento,
    COUNT(t.id) as total_tickets,
    COUNT(CASE WHEN t.estado IN (1, 2) THEN t.id END) as tickets_vendidos,
    -- Campos adicionales útiles
    COUNT(CASE WHEN t.estado = 0 THEN t.id END) as tickets_disponibles,
    ROUND((COUNT(CASE WHEN t.estado IN (1, 2) THEN t.id END) / COUNT(t.id)) * 100, 2) as porcentaje_vendido
FROM localidades l
JOIN dias_localidades dl ON l.id = dl.localidad_id
JOIN dias d ON dl.dia_id = d.id
JOIN eventos e ON d.evento_id = e.id
LEFT JOIN tickets t ON t.localidad_id = l.id
GROUP BY l.id
HAVING COUNT(t.id) > 0  -- Solo localidades con tickets
ORDER BY porcentaje_vendido DESC LIMIT 10;


USE db_microservicio_reporte_sensor;

CREATE OR REPLACE VIEW localidades_por_acabar AS
SELECT
    l.nombre as nombre,
    l.id as localidad_id,
    e.nombre as evento,
    COUNT(t.id) as total_tickets,
    COUNT(CASE WHEN t.estado IN (1, 2) THEN t.id END) as tickets_vendidos,
    -- Campos adicionales útiles
    COUNT(CASE WHEN t.estado = 0 THEN t.id END) as tickets_disponibles,
    CAST(ROUND((COUNT(CASE WHEN t.estado IN (1, 2) THEN t.id END) / COUNT(t.id)) * 100, 0) AS SIGNED) as porcentaje_vendido
FROM localidades l
JOIN dias_localidades dl ON l.id = dl.localidad_id
JOIN dias d ON dl.dia_id = d.id
JOIN eventos e ON d.evento_id = e.id
LEFT JOIN tickets t ON t.localidad_id = l.id
GROUP BY l.id
HAVING COUNT(t.id) > 0  -- Solo localidades con tickets
ORDER BY porcentaje_vendido DESC LIMIT 10;