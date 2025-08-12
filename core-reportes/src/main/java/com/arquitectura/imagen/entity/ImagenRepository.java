package com.arquitectura.imagen.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagenRepository extends JpaRepository<Imagen, Long>{

    List<Imagen> findByEventoId(Long eventoId);

    List<Imagen> findByTipo(int tipo);

    List<Imagen> findByEventoIdAndTipo(Long eventoId, int tipo);

}
