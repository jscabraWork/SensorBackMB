package com.arquitectura.reporte.service;

import com.arquitectura.reporte.view.VentaPromorView;
import com.arquitectura.reporte.view.VentaPromotorViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportePromotorServiceImpl implements ReportePromotorService{

    @Autowired
    private VentaPromotorViewRepository repository;

    @Override
    public List<VentaPromorView> findByEventoId(Long eventoId) {
        return repository.findByEventoId(eventoId);
    }

    @Override
    public VentaPromorView findByDocumentoAndEventoId(String documento, Long eventoId) {
        return repository.findByDocumentoAndEventoId(documento, eventoId);
    }
}
