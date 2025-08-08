package com.arquitectura.dia.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaRepository extends JpaRepository<Dia,Long> {

    List<Dia> findAllByEstadoAndEventoId(int estado, Long eventoId);

    List<Dia> findAllByEventoId(Long eventoId);
}
