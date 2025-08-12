package com.arquitectura.evento.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.dia.entity.Dia;
import com.arquitectura.dia.service.DiaService;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.promotor.service.PromotorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private PromotorService promotorService;

    @Autowired
    private DiaService diaService;

    @GetMapping("/listar-estado/{estado}")
    public ResponseEntity<?> getEventosByEstado(@PathVariable Integer estado) {
        Map<String, Object> response = new HashMap<>();
        List<Evento> eventos = service.findByEstado(estado);
        response.put("eventos", eventos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/listar-no-estado/{estado}")
    public ResponseEntity<?> getEventosByNoEstado(@PathVariable Integer estado) {
        Map<String, Object> response = new HashMap<>();
        List<Evento> eventos = service.findByNoEstado(estado);
        response.put("eventos", eventos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/activos-promotor/{pPromotorId}")
    public ResponseEntity<?> getEventosActivosDePromotor(@PathVariable String pPromotorId) {
        Map<String, Object> response = new HashMap<>();
        Promotor promotor = promotorService.findById(pPromotorId);
        List<Evento> eventos = service.findByPromotoresNumeroDocumentoAndNoEstado(pPromotorId,3);
        response.put("promotor", promotor);
        response.put("eventos", eventos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/historial-promotor/{pPromotorId}")
    public ResponseEntity<?> getEventosHistoricosDePromotor(@PathVariable String pPromotorId) {
        Map<String, Object> response = new HashMap<>();
        Promotor promotor = promotorService.findById(pPromotorId);
        List<Evento> eventos = service.findByPromotoresNumeroDocumentoAndEstado(pPromotorId,3);
        response.put("promotor", promotor);
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
