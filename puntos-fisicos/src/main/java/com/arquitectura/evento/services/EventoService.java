package com.arquitectura.evento.services;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.services.CommonService;

import java.util.List;

public interface EventoService extends CommonService<Evento> {

    List<Evento> findByPuntoFisicoId(String promotorId);

    List<Evento> findByEstado(Integer estado);

    List<Evento> findByNoEstado(Integer estado);


    List<Evento> findByPuntosFisicosNumeroDocumentoAndNoEstado(String numeroDocumento, Integer estado);


}
