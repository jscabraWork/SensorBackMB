package com.arquitectura.temporada.services;

import com.arquitectura.services.CommonServiceImpl;
import com.arquitectura.temporada.entity.Temporada;
import com.arquitectura.temporada.entity.TemporadaRepository;
import org.springframework.stereotype.Service;

@Service
public class TemporadaServiceImpl extends CommonServiceImpl<Temporada, TemporadaRepository> implements TemporadaService {
}
