package com.arquitectura.alcancia.service;

import com.arquitectura.alcancia.entity.Alcancia;
import com.arquitectura.alcancia.entity.AlcanciaRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlcanciaServiceImpl extends CommonServiceImpl<Alcancia, AlcanciaRepository> implements AlcanciaService {


    @Override
    public List<Alcancia> findByEventoIdAndEstado(Long eventoId, Integer estado) {
        return repository.findByEventoIdAndEstado(eventoId, estado);
    }
}
