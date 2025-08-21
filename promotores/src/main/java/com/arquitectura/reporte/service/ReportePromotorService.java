package com.arquitectura.reporte.service;

import com.arquitectura.reporte.view.VentaPromorView;

import java.util.List;

public interface ReportePromotorService {

    List<VentaPromorView> findByEventoId(Long eventoId);

    VentaPromorView findByDocumentoAndEventoId(String documento, Long eventoId);

}
