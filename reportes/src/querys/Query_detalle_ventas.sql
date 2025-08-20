use db_microservicio_reporte_sensor;

-- query inicial
SELECT ta.nombre as nombreTarifa, 
ta.precio as precio, 
ta.servicio as servicio, 
ta.iva as iva, 
d.nombre as nombreDia,
l.nombre as nombreLocalidad FROM tickets t 
join tarifas ta on t.tarifa_id = ta.id
join localidades l on ta.localidad_id =l.id
join dias_localidades dl on dl.localidad_id = l.id
join dias d on dl.dia_id = d.id
where d.evento_id =1 group by ta.id order by ta.nombre;

-- query completa optimizada
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
    WHERE d.evento_id = 1
    GROUP BY d.evento_id, l.id
)
SELECT 
    ta.nombre as tarifa,
    GROUP_CONCAT(DISTINCT l.nombre) as localidad,
    GROUP_CONCAT(DISTINCT d.nombre) as dia,
    ta.id as tarifaId,
    ta.localidad_id as localidadId,
    GROUP_CONCAT(DISTINCT d.id) as diaId,
    ta.precio, ta.servicio, ta.iva,
    
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
WHERE d.evento_id = 1
GROUP BY ta.id
ORDER BY ta.nombre;