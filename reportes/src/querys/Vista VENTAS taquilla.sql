use db_microservicio_puntosfisicos_sensor;

SELECT
    ep.eventos_id as evento_id,
    p.numero_documento as documento,
    p.correo,
    p.nombre,
    COUNT(DISTINCT CASE WHEN d.evento_id = ep.eventos_id THEN t.id END) as cantidad_vendida,
    COALESCE(SUM(CASE WHEN d.evento_id = ep.eventos_id THEN ta.precio + ta.servicio + ta.iva END), 0) as recaudado,
    COALESCE(SUM(CASE WHEN d.evento_id = ep.eventos_id THEN ta.precio END), 0) as recaudado_precio,
    COALESCE(SUM(CASE WHEN d.evento_id = ep.eventos_id THEN ta.servicio END), 0) as recaudado_servicio,
    COALESCE(SUM(CASE WHEN d.evento_id = ep.eventos_id THEN ta.iva END), 0) as recaudado_iva
FROM puntosfisicos p
JOIN eventos_puntosfisicos ep ON ep.puntofisico_numero_documento = p.numero_documento
LEFT JOIN tickets t ON p.numero_documento = t.puntofisico_id AND t.estado = 1
LEFT JOIN tarifas ta ON t.tarifa_id = ta.id
LEFT JOIN localidades l ON t.localidad_id = l.id
LEFT JOIN dias_localidades dl ON dl.localidad_id = l.id
LEFT JOIN dias d ON dl.dia_id = d.id
GROUP BY ep.eventos_id, p.numero_documento;

CREATE OR REPLACE VIEW ventas_taquilla AS
SELECT
   ROW_NUMBER() OVER() as id,
   ep.eventos_id as evento_id,
   p.numero_documento as documento,
   p.correo,
   p.nombre,
   COUNT(DISTINCT CASE WHEN d.evento_id = ep.eventos_id THEN t.id END) as cantidad_vendida,
   COALESCE(SUM(CASE WHEN d.evento_id = ep.eventos_id THEN ta.precio + ta.servicio + ta.iva END), 0) as recaudado,
   COALESCE(SUM(CASE WHEN d.evento_id = ep.eventos_id THEN ta.precio END), 0) as recaudado_precio,
   COALESCE(SUM(CASE WHEN d.evento_id = ep.eventos_id THEN ta.servicio END), 0) as recaudado_servicio,
   COALESCE(SUM(CASE WHEN d.evento_id = ep.eventos_id THEN ta.iva END), 0) as recaudado_iva
FROM puntosfisicos p
JOIN eventos_puntosfisicos ep ON ep.puntofisico_numero_documento = p.numero_documento
LEFT JOIN tickets t ON p.numero_documento = t.puntofisico_id AND t.estado = 1
LEFT JOIN tarifas ta ON t.tarifa_id = ta.id
LEFT JOIN localidades l ON t.localidad_id = l.id
LEFT JOIN dias_localidades dl ON dl.localidad_id = l.id
LEFT JOIN dias d ON dl.dia_id = d.id
GROUP BY ep.eventos_id, p.numero_documento;