package com.arquitectura.alcancia.service;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.services.CommonService;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AlcanciaService extends CommonService<Alcancia> {

    List<Alcancia> findByEventoIdAndEstado(Long eventoId,Integer estado);
}
