package com.arquitectura.recuperacion.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecuperacionRepository extends JpaRepository<Recuperacion, Long>{

	public Recuperacion findByIdBusqueda(String pIdBusqueda);
}
