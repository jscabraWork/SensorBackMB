package com.arquitectura.evento.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.services.DiaService;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.services.EventoService;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.puntofisico.services.PuntoFisicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/eventos")
public class EventoController extends CommonController<Evento, EventoService> {

    @Autowired
    private PuntoFisicoService puntoFisicoService;

    @Autowired
    private DiaService diaService;

    @GetMapping("/listar-no-estado/{estado}")
    public ResponseEntity<?> getEventos(@PathVariable Integer estado) {
        Map<String, Object> response = new HashMap<>();
        List<Evento> eventos = service.findByNoEstado(3);
        response.put("eventos", eventos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/activos/{pPuntoId}")
    public ResponseEntity<?> getEventosActivosDePromotor(@PathVariable String pPuntoId) {
        Map<String, Object> response = new HashMap<>();
        PuntoFisico puntoFisico = puntoFisicoService.findById(pPuntoId);
        List<Evento> eventos = service.findByPuntosFisicosNumeroDocumentoAndNoEstado(pPuntoId,3);
        response.put("taquilla", puntoFisico);
        response.put("eventos", eventos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/perfil/{pEventoId}")
    public ResponseEntity<?> getEventoPerfil(@PathVariable Long pEventoId) {
        Map<String, Object> response = new HashMap<>();
        Evento evento = service.findById(pEventoId);

        // Estado 1 para activos
        //el dia trae todas sus localidades y localidades trae su tarifa activa
        List<Dia> dias = diaService.findByEventoIdAndEstado(pEventoId, 1);

        response.put("dias",dias);
        response.put("evento", evento);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
