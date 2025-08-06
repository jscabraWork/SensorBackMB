package com.arquitectura.intento_registro.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentoRegistroRepository extends JpaRepository<IntentoRegistro, Long> {

    public IntentoRegistro findByIdBusqueda(String pIdBusqueda);
}
