package com.arquitectura.organizador.controller;
import com.arquitectura.controller.CommonControllerString;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.organizador.entity.Organizador;
import com.arquitectura.organizador.service.OrganizadorService;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.views.graficas.service.GraficaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/organizador")
public class OrganizadorController extends CommonControllerString<Organizador, OrganizadorService> {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private GraficaService graficaService;

    @GetMapping("/eventos/{pOrganizadorId}")
    public ResponseEntity<?> getEventosActivosByOrganizador(@PathVariable String pOrganizadorId) {
        Map<String, Object> response = new HashMap<>();
        List<Evento> eventos = eventoService.findByOrganizadoresNumeroDocumentoAndEstadoNot(pOrganizadorId,3);
        response.put("eventos", eventos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/terminados/{pOrganizadorId}")
    public ResponseEntity<?> getEventosTerminadosByOrganizador(@PathVariable String pOrganizadorId) {
        Map<String, Object> response = new HashMap<>();
        List<Evento> eventos = eventoService.findByOrganizadoresNumeroDocumentoAndEstado(pOrganizadorId,3);
        response.put("eventos", eventos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/resumen/{pEventoId}")
    public ResponseEntity<?> getEventosTerminadosByOrganizador(@PathVariable Long pEventoId) {
        Map<String, Object> response = new HashMap<>();
        Evento evento = eventoService.findById(pEventoId);

        if (evento == null) {
            response.put("message", "Evento no encontrado");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        //Obtener el año del evento
        Integer anio = evento.getFechaApertura().getYear();

        response.put("resumen", eventoService.getResumenByEventoId(pEventoId));
        response.put("graficaCircular", graficaService.getGraficaDineroRecaudadoByMetodo(pEventoId, null, anio));
        response.put("graficaLineas", graficaService.getGraficaLineaVentas(pEventoId, null, anio));
        response.put("evento", evento);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }





}