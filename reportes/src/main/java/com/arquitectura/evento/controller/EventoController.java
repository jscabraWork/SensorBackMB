package com.arquitectura.evento.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eventos")
public class EventoController  extends CommonController<Evento, EventoService> {
}
