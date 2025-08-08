package com.arquitectura.localidad.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocalidadRepository extends JpaRepository<Localidad,Long> {


    List<Localidad> findAllByNombreIgnoreCase(String nombre);

    List<Localidad> findAllByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Localidad> findByDiasId(Long diaId);

    List<Localidad> findByDiasEventoId(Long pEventoId);

    List<Localidad> findByDiasEventoIdAndDiasEstado(Long pEventoId, int estado);

}
