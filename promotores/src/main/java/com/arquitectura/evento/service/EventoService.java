package com.arquitectura.evento.service;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.services.CommonService;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventoService extends CommonService<Evento> {

    List<Evento> findByPromotorId(String promotorId);

    List<Evento> findByEstado(Integer estado);

    List<Evento> findByNoEstado(Integer estado);

    List<Evento> findByPromotoresNumeroDocumentoAndEstado(String numeroDocumento, Integer estado);

    List<Evento> findByPromotoresNumeroDocumentoAndNoEstado(String numeroDocumento, Integer estado);


}
