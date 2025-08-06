package com.arquitectura.codigo_validacion.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodigoRepository extends JpaRepository<Codigo, Long>{

	public Codigo findByIdBusqueda(String pIdBusqueda);
}
