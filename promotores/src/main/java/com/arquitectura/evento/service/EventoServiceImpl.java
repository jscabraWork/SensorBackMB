package com.arquitectura.evento.service;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoServiceImpl extends CommonServiceImpl<Evento, EventoRepository> implements EventoService {

    @Override
    public List<Evento> findByPromotorId(String promotorId) {
        return repository.findByPromotorId(promotorId);
    }

    @Override
    public List<Evento> findByEstado(Integer estado) {
        return repository.findByEstado(estado);
    }

    @Override
    public List<Evento> findByNoEstado(Integer estado) {
        return repository.findByEstadoNot(estado);
    }

    @Override
    public List<Evento> findByPromotoresNumeroDocumentoAndEstado(String numeroDocumento, Integer estado) {
        return repository.findByPromotoresNumeroDocumentoAndEstado(numeroDocumento, estado);
    }

    @Override
    public List<Evento> findByPromotoresNumeroDocumentoAndNoEstado(String numeroDocumento, Integer estado) {
        return repository.findByPromotoresNumeroDocumentoAndEstadoNot(numeroDocumento, estado);

    }
}
