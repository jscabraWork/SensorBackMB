package com.arquitectura.evento.service;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.entity.EventoRepository;
import com.arquitectura.services.CommonServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EventoServiceImpl extends CommonServiceImpl<Evento, EventoRepository> implements EventoService {
}
