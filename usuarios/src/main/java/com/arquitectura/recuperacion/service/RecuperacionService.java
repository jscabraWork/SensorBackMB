package com.arquitectura.recuperacion.service;

import com.arquitectura.recuperacion.entity.Recuperacion;

public interface RecuperacionService {

	public Recuperacion crearRecuperacion(String correo) throws Exception;
	
	public Recuperacion getRecuperacion(String idBusqueda);
	
	public Recuperacion save(Recuperacion pRecuperacion);
}
