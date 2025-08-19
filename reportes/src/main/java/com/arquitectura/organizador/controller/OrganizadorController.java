package com.arquitectura.organizador.controller;
import com.arquitectura.controller.CommonControllerString;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.organizador.entity.Organizador;
import com.arquitectura.organizador.service.OrganizadorService;
import com.arquitectura.puntofisico.entity.PuntoFisico;
import com.arquitectura.views.detalle_evento.DetalleEventoView;
import com.arquitectura.views.graficas.service.GraficaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/organizadores")
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
    public ResponseEntity<?> getEventosTerminadosByOrganizador(@PathVariable Long pEventoId,
                                                              @RequestParam(required = false) Integer anio,
                                                              @RequestParam(required = false) Integer mes) {
        Map<String, Object> response = new HashMap<>();
        Evento evento = eventoService.findById(pEventoId);

        if (evento == null) {
            response.put("message", "Evento no encontrado");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        // Si no se proporciona el año como parámetro, usar la lógica existente
        if (anio == null) {
            //Obtener el año del evento
            anio = evento.getFechaApertura().getYear();
            Integer anioActual = Year.now().getValue();

            //Si el año del evento es mayor al año actual, se asigna el año actual para las consultas de las graficas
            if(anio > anioActual) {
                anio = anioActual;
            }
        }

        // Si no se proporciona el mes como parámetro, usar -1 (todos los meses)
        if (mes == null) {
            mes = -1;
        }

        response.put("resumen", eventoService.getResumenByEventoId(pEventoId));
        response.put("graficaCircular", graficaService.getGraficaDineroRecaudadoByMetodo(pEventoId, mes, anio));
        response.put("graficaLineas", graficaService.getGraficaLineaVentas(pEventoId, mes, anio));
        response.put("evento", evento);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @GetMapping("/detalle-ventas/{pEventoId}")
    public ResponseEntity<?> getDetalleVentasEvento(@PathVariable Long pEventoId,
                                                    @RequestParam(required = false) Long tarifaId,
                                                    @RequestParam(required = false) Long localidadId,
                                                    @RequestParam(required = false) Long diaId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validar que el evento existe
            Evento evento = eventoService.findById(pEventoId);
            if (evento == null) {
                response.put("message", "Evento no encontrado");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Obtener detalle de ventas con filtros opcionales
            List<DetalleEventoView> detalleVentas = eventoService.getDetalleEvento(pEventoId, tarifaId, localidadId, diaId);

            // Construir respuesta
            response.put("evento", evento);
            response.put("detalle", detalleVentas);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("message", "Error al obtener detalle de ventas");
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}