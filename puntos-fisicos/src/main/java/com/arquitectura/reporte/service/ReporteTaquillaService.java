package com.arquitectura.reporte.service;

import com.arquitectura.reporte.view.VentaTaquillaView;

import java.util.List;

public interface ReporteTaquillaService {

    List<VentaTaquillaView> findByEventoId(Long eventoId);

    VentaTaquillaView findByDocumentoAndEventoId(String documento, Long eventoId);

}
