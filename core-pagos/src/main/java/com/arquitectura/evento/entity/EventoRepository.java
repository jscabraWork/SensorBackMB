package com.arquitectura.evento.entity;

import com.arquitectura.dia.entity.Dia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento,Long> {

    List<Evento> findAllByEstado(int estado);

    public Evento findByIdAndEstadoIn(Long pId, List<Integer> pEstado);

}
