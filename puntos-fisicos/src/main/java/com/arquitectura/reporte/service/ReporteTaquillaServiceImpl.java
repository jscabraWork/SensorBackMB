package com.arquitectura.reporte.service;

import com.arquitectura.reporte.view.VentaTaquillaView;
import com.arquitectura.reporte.view.VentaTaquillaViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteTaquillaServiceImpl implements ReporteTaquillaService {

    @Autowired
    private VentaTaquillaViewRepository repository;

    @Override
    public List<VentaTaquillaView> findByEventoId(Long eventoId) {
        return repository.findByEventoId(eventoId);
    }

    @Override
    public VentaTaquillaView findByDocumentoAndEventoId(String documento, Long eventoId) {
        return repository.findByDocumentoAndEventoId(documento, eventoId);
    }
}
