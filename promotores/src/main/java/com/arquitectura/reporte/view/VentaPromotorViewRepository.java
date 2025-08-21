package com.arquitectura.reporte.view;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaPromotorViewRepository extends JpaRepository<Long, VentaPromorView> {

    List<VentaPromorView> findByEventoId(Long eventoId);

    VentaPromorView findByDocumentoAndEventoId(String documento, Long eventoId);

}
