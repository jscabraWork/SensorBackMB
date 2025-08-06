package com.arquitectura.puntofisico.controller;

import com.arquitectura.controller.CommonControllerString;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.services.EventoService;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.puntofisico.services.PuntoFisicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/puntosfisicos")
public class PuntoFisicoController extends CommonControllerString<PuntoFisico, PuntoFisicoService> {

    @Autowired
    private EventoService eventoService;

    @GetMapping("/puntofisico/{pNumeroDocumento}")
    public ResponseEntity<?> getPuntoFisicoById(@PathVariable String pNumeroDocumento){
        Map<String, Object> response = new HashMap<>();
        PuntoFisico puntoFisico = service.findById(pNumeroDocumento);
        if (puntoFisico != null) {
            response.put("puntofisico", puntoFisico);
            response.put("eventosAsignados", puntoFisico.getEventos());
            response.put("eventos", eventoService.findByNoEstado(3));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Punto físico no encontrado");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/evento/{pEventoid}")
    public ResponseEntity<?> getPuntosFisicosByEventoId(@PathVariable Long pEventoid) {
        Map<String, Object> response = new HashMap<>();
        List<PuntoFisico> puntosFisicos = service.findByEvento(pEventoid);
        Evento evento = eventoService.findById(pEventoid);
        response.put("puntosFisicos", puntosFisicos);
        response.put("evento", evento);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/asignar-eventos/{numeroDocumento}")
    public ResponseEntity<?> asignarEventos(@PathVariable String numeroDocumento, @RequestBody List<Long> pEventoid) {
        Map<String, Object> response = new HashMap<>();
        PuntoFisico puntoFisico = service.asignarEventos(numeroDocumento, pEventoid);
        response.put("puntoFisico", puntoFisico);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/filtrar")
    public ResponseEntity<?> filtrarPuntosFisicos(@RequestParam(required = false) String nombre,
                                               @RequestParam(required = false) String numeroDocumento,
                                               @RequestParam(required = false) String correo) {
        Map<String, Object> response = new HashMap<>();
        List<PuntoFisico> puntos = service.findByFiltro(nombre, numeroDocumento, correo);
        response.put("puntosFisicos", puntos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
