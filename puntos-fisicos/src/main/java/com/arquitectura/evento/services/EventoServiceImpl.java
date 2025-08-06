package com.arquitectura.evento.services;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoServiceImpl extends CommonServiceImpl<Evento, EventoRepository> implements EventoService {

    @Override
    public List<Evento> findByPuntoFisicoId(String puntofisicoId) {
        return repository.findByPuntoFisicoId(puntofisicoId);
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
    public List<Evento> findByPuntosFisicosNumeroDocumentoAndNoEstado(String numeroDocumento, Integer estado) {
        return repository.findByPuntosFisicosNumeroDocumentoAndEstadoNot(numeroDocumento, estado);
    }
}
