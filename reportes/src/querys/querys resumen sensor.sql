use db_microservicio_reporte_sensor;

-- Total de asistentes al eventos, cuenta todos los tickets vendidos
-- variable evento_id
select count(distinct(t.id)) as asistentes 
from tickets t join localidades l on t.localidad_id = l.id
join dias_localidades dl on dl.localidad_id = l.id
join dias d on dl.dia_id = d.id
join eventos e on d.evento_id = e.id
where e.id =1 and t.estado =1;

select count(distinct(i.id)) as ingresos
from tickets t join localidades l on t.localidad_id = l.id
join ingresos i on i.ticket_id = t.id
join dias_localidades dl on dl.localidad_id = l.id
join dias d on dl.dia_id = d.id
join eventos e on d.evento_id = e.id
where e.id =1 and i.utilizado = true;

-- Calcular total recaudado en el evento
-- variable evento_id
SELECT SUM(tr.precio + tr.servicio + tr.iva) as totalRecaudado FROM tickets t 
JOIN localidades l ON t.localidad_id = l.id
JOIN tarifas tr ON tr.id = t.tarifa_id
WHERE l.id IN ( SELECT DISTINCT dl.localidad_id FROM dias_localidades dl
JOIN dias d ON dl.dia_id = d.id WHERE d.evento_id = 1) AND t.estado = 1;

-- Calcular total servicio e iva del evento
SELECT SUM(tr.servicio) as servicioRecaudado FROM tickets t 
JOIN localidades l ON t.localidad_id = l.id
JOIN tarifas tr ON tr.id = t.tarifa_id
WHERE l.id IN ( SELECT DISTINCT dl.localidad_id FROM dias_localidades dl
JOIN dias d ON dl.dia_id = d.id WHERE d.evento_id = 1) AND t.estado = 1;

SELECT SUM(tr.precio) as precioRecaudado FROM tickets t 
JOIN localidades l ON t.localidad_id = l.id
JOIN tarifas tr ON tr.id = t.tarifa_id
WHERE l.id IN ( SELECT DISTINCT dl.localidad_id FROM dias_localidades dl
JOIN dias d ON dl.dia_id = d.id WHERE d.evento_id = 1) AND t.estado = 1;

SELECT SUM(tr.iva) as ivaRecaudado FROM tickets t 
JOIN localidades l ON t.localidad_id = l.id
JOIN tarifas tr ON tr.id = t.tarifa_id
WHERE l.id IN ( SELECT DISTINCT dl.localidad_id FROM dias_localidades dl
JOIN dias d ON dl.dia_id = d.id WHERE d.evento_id = 1) AND t.estado = 1;

-- Contar cortesias del evento
-- variable evento_id
select count(distinct(t.id)) as cortesias from tickets t 
join localidades l on t.localidad_id = l.id
join tarifas tr on tr.id = t.tarifa_id
join dias_localidades dl on dl.localidad_id = l.id
join dias d on dl.dia_id = d.id
join eventos e on d.evento_id = e.id
where e.id =1 and t.estado =1 and tr.precio = 0;


-- Contar asistentes por taquilla por evento
-- variable evento_id
select count(distinct(t.id)) as asistentesTaquilla from tickets t 
join orden_tickets ot on ot.ticket_id = t.id
join ordenes_puntosfisicos op on op.id = ot.orden_id
join ordenes o on op.id = o.id
where o.estado = 1 and t.estado =1 and o.evento_id = 1;

-- Contar pagos por taquilla por evento
-- variable evento_id
select count(distinct(t.id)) as pagosTaquilla from  
ordenes_puntosfisicos op 
join ordenes o on op.id = o.id
where o.estado = 1 and o.evento_id = 1;

-- Contar transacciones aprobadas por método y evento
-- variables metodo y evento_id
-- metodo 1: Tarjeta
-- metodo 2: PSE
select count(distinct(tr.id)) as pagosPSE from transacciones tr
join ordenes o on o.id = tr.orden_id
where tr.status = 34 and o.evento_id = 1 and metodo = 2;

select count(distinct(tr.id)) as pagosTC from transacciones tr
join ordenes o on o.id = tr.orden_id
where tr.status = 34 and o.evento_id = 1 and metodo = 1;

select count(distinct(tr.id)) as totalTransacciones from transacciones tr
join ordenes o on o.id = tr.orden_id
where tr.status = 34 and o.evento_id = 1;

select count(distinct(o.cliente_id)) as totalCompradores from transacciones tr
join ordenes o on o.id = tr.orden_id
where tr.status = 34 and o.evento_id = 1;

-- Contar total de asistentes por método y evento
-- variables metodo y evento_id
select count(distinct(t.id)) as asistentesPSE from transacciones tr
join ordenes o on o.id = tr.orden_id
join orden_tickets ot on o.id = ot.orden_id
join tickets t on t.id = ot.ticket_id
where o.estado =1 and o.evento_id = 1 and metodo = 2;

select count(distinct(t.id)) as asistentesTC from transacciones tr
join ordenes o on o.id = tr.orden_id
join orden_tickets ot on o.id = ot.orden_id
join tickets t on t.id = ot.ticket_id
where o.estado =1 and o.evento_id = 1 and metodo = 1;

-- Contar tickets vendidos el dia de hoy 
-- variable evento_id
-- Excluye órdenes tipo 5
select count(distinct(t.id)) as cantidadHoy from tickets t 
join orden_tickets ot on ot.ticket_id = t.id
join ordenes o on ot.orden_id = o.id
where o.estado = 1 and t.estado =1 and o.evento_id = 1 and o.tipo != 5
and DATE(o.creation_date) = date(now());

-- Total recaudado hoy
-- Excluye órdenes tipo 5
select sum(tr.precio + tr.servicio + tr.iva) as totalHoy 
from tickets t JOIN tarifas tr ON tr.id = t.tarifa_id
join orden_tickets ot on ot.ticket_id = t.id
join ordenes o on ot.orden_id = o.id
where o.estado = 1 and t.estado =1 and o.evento_id = 1 and o.tipo != 5
and DATE(o.creation_date) = date(now());

-- IMPUESTOS --

-- RETEFUENTE por evento
-- variable evento_id
-- 1.5% de todas las transacciones aprobadas por tarjeta
-- Excluye órdenes tipo 5
select sum(tr.amount) * 0.015 as retefuente from transacciones tr
join ordenes o on o.id = tr.orden_id
where tr.status = 34 and o.evento_id = 1 and metodo = 1 and o.tipo != 5;

-- RETEICA por evento
-- variable evento_id
-- 0.414% de todas las transacciones aprobadas por tarjeta
-- Excluye órdenes tipo 5
select sum(tr.amount) * 0.00414 as reteiva from transacciones tr
join ordenes o on o.id = tr.orden_id
where tr.status = 34 and o.evento_id = 1 and metodo = 1 and o.tipo != 5;

-- Calcular parafiscal por evento
-- Excluye órdenes tipo 5
select SUM(tr.precio + tr.servicio + tr.iva) * 0.1 as parafiscal
from ordenes o join orden_tickets ot on o.id = ot.orden_id
join tickets t on t.id = ot.ticket_id
join tarifas tr on tr.id = t.tarifa_id
join uvt on uvt.ano = year(o.creation_date) -- se calcula con uvt del año de la orden
where o.estado =1 and o.evento_id = 1 and o.tipo != 5
and (tr.precio + tr.servicio + tr.iva)  >= uvt.valor; -- Solo se cuenta si el precio del ticket es mayor al precio del parafiscal

----- COMISIONES -----

-- COMISION ALLTICKETS
-- 5% de todas las transacciones aprobadas mas iva
-- Excluye órdenes tipo 5
SELECT coalesce((SUM(tr.amount) * c.valor),0) * 1.19 AS comisionAT
FROM transacciones tr JOIN ordenes o ON o.id = tr.orden_id
JOIN comisiones c ON c.concepto = 'comisionAT'
WHERE tr.status = 34 AND o.evento_id = 1 AND o.tipo != 5;

-- COMISION PASARRELA
-- 368 pesos por cantidad de transacciones aprobadas en pasarela
-- Excluye órdenes tipo 5
SELECT coalesce((count(distinct(tr.id)) * c.valor),0) AS comisionPasarela
FROM transacciones tr JOIN ordenes o ON o.id = tr.orden_id
JOIN comisiones c ON c.concepto = 'pasarela'
WHERE tr.status = 34 and tr.metodo in (1,2)
AND o.evento_id = 1 AND o.tipo != 5;


-- COMISION 3DS
-- 367 pesos por cantidad de transacciones aprobadas o no por TC
-- Excluye órdenes tipo 5
SELECT coalesce((count(distinct(tr.id)) * c.valor),0) AS comision3DS
FROM transacciones tr JOIN ordenes o ON o.id = tr.orden_id
JOIN comisiones c ON c.concepto = '3ds'
WHERE tr.metodo =1 AND o.evento_id = 1 AND o.tipo != 5;