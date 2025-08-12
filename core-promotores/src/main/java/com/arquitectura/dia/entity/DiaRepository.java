package com.arquitectura.dia.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaRepository extends JpaRepository<Dia, Long> {

    List<Dia> findByEventoId(Long eventoId);

    List<Dia> findByEventoIdAndEstado(Long eventoId, Integer estado);
}


