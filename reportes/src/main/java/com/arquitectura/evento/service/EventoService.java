package com.arquitectura.evento.service;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.services.CommonService;
import com.arquitectura.views.resumen_evento.ResumenEventoView;

import java.util.List;

public interface EventoService extends CommonService<Evento> {

    List<Evento> findByOrganizadoresNumeroDocumentoAndEstadoNot(String numeroDocumento, Integer pEstado);

    List<Evento> findByOrganizadoresNumeroDocumentoAndEstado(String numeroDocumento, Integer pEstado);

    public ResumenEventoView findResumenByEventoId(Long id);
}
