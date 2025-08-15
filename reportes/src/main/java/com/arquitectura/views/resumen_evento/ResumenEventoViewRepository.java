package com.arquitectura.views.resumen_evento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumenEventoViewRepository extends JpaRepository<ResumenEventoView, Long> {

    Optional<ResumenEventoView> findByEventoId(Long eventoId);

}
