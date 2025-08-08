package com.arquitectura.tipo.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.events.TemporadaEvent;
import com.arquitectura.temporada.entity.Temporada;
import com.arquitectura.tipo.entity.Tipo;
import com.arquitectura.tipo.service.TipoService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Uuid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/tipos")
public class TipoController extends CommonController<Tipo, TipoService> {



}
