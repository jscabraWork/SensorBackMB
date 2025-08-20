use db_microservicio_reporte_sensor;
CREATE OR REPLACE VIEW detalle_ventas AS
WITH localidad_stats AS (
    SELECT 
        d.evento_id,
        l.id as localidad_id,
        COUNT(CASE WHEN t.estado = 0 THEN 1 END) as cantidadDisponible,
        COUNT(CASE WHEN t.estado = 3 THEN 1 END) as cantidadEnProceso,
        COUNT(t.id) as totalTicketsLocalidad
    FROM localidades l
    LEFT JOIN tickets t ON t.localidad_id = l.id
    JOIN dias_localidades dl ON dl.localidad_id = l.id
    JOIN dias d ON dl.dia_id = d.id
    GROUP BY d.evento_id, l.id
)
SELECT 
    d.evento_id as eventoId,
    ta.nombre as tarifa,
    l.nombre as localidad,
    d.nombre as dia,
    ta.id as tarifaId,
    ta.localidad_id as localidadId,
    d.id as diaId,  -- Ahora es un número individual
    ta.precio, ta.servicio, ta.iva,
    (ta.precio+ta.servicio+ta.iva) as precioTotal,
    
    COUNT(CASE WHEN t.estado = 1 THEN 1 END) as vendidos,
    COUNT(CASE WHEN t.estado = 2 THEN 1 END) as reservados,
    
    COALESCE(MAX(ls.cantidadEnProceso), 0) as proceso,
    COALESCE(MAX(ls.cantidadDisponible), 0) as disponibles,
    COALESCE(MAX(ls.totalTicketsLocalidad), 0) as totalTickets,
    
    COUNT(CASE WHEN t.estado = 1 THEN 1 END) * ta.precio as totalPrecio,
    COUNT(CASE WHEN t.estado = 1 THEN 1 END) * ta.servicio as totalServicio,
    COUNT(CASE WHEN t.estado = 1 THEN 1 END) * ta.iva as totalIva,
    COUNT(CASE WHEN t.estado = 1 THEN 1 END) * (ta.precio + ta.servicio + ta.iva) as totalRecaudado
FROM tarifas ta
JOIN localidades l ON ta.localidad_id = l.id
JOIN dias_localidades dl ON dl.localidad_id = l.id
JOIN dias d ON dl.dia_id = d.id
LEFT JOIN tickets t ON t.tarifa_id = ta.id AND t.estado IN (1, 2)
LEFT JOIN localidad_stats ls ON ls.localidad_id = ta.localidad_id AND ls.evento_id = d.evento_id
GROUP BY d.evento_id, ta.id, d.id  -- Agregar d.id al GROUP BY
ORDER BY d.evento_id, ta.nombre, d.id;