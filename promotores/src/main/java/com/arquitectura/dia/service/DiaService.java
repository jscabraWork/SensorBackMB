package com.arquitectura.dia.service;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.services.CommonService;

import java.util.List;

public interface DiaService extends CommonService<Dia> {

    List<Dia> findByEventoIdAndEstado(Long eventoId, Integer estado);


}
