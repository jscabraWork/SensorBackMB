package com.arquitectura.evento.service;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoServiceImpl extends CommonServiceImpl<Evento, EventoRepository> implements EventoService {

    @Override
    public List<Evento> findByOrganizadoresNumeroDocumentoAndEstadoNot(String numeroDocumento, Integer pEstado) {
        return repository.findByOrganizadoresNumeroDocumentoAndEstadoNot(numeroDocumento, pEstado);
    }

    @Override
    public List<Evento> findByOrganizadoresNumeroDocumentoAndEstado(String numeroDocumento, Integer pEstado) {
        return repository.findByOrganizadoresNumeroDocumentoAndEstado(numeroDocumento, pEstado);
    }
}
