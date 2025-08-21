package com.arquitectura.reporte.view;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaTaquillaViewRepository extends JpaRepository<VentaTaquillaView, Long> {

    List<VentaTaquillaView> findByEventoId(Long eventoId);

    VentaTaquillaView findByDocumentoAndEventoId(String documento, Long eventoId);

}
