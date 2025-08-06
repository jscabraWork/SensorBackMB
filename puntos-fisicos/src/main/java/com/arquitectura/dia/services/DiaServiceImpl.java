package com.arquitectura.dia.services;

import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.entity.DiaRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiaServiceImpl extends CommonServiceImpl<Dia,DiaRepository> implements DiaService {

    @Override
    public List<Dia> findByEventoIdAndEstado(Long eventoId, Integer estado) {
        return repository.findByEventoIdAndEstado(eventoId, estado);
    }

}
