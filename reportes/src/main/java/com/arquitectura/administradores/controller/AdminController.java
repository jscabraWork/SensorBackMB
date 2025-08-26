package com.arquitectura.administradores.controller;

import com.arquitectura.administradores.service.AdminService;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.organizador.service.ReporteService;
import com.arquitectura.views.graficas.service.GraficaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService reporteService;

    @Autowired
    private GraficaService graficaService;

    @Autowired
    private EventoService eventoService;

    @GetMapping("/resumen")
    public ResponseEntity<?> getResumenAdmin(@RequestParam(required = false) Long pEventoId,
                                                               @RequestParam(required = false) Integer pAnio,
                                                               @RequestParam(required = false) Integer pMes,
                                                               @RequestParam(required = false) Integer pDia) {
        Map<String, Object> response = new HashMap<>();
        // Si no se proporciona el año como parámetro, usar la lógica existente
        if (pAnio == null) {
            Integer anioActual = Year.now().getValue();
            pAnio = anioActual;
        }

        response.put("eventos", eventoService.findByEstado(2)); // Lista de eventos visibles
        response.put("localidades", reporteService.getLocalidadesPorAcabar());
        response.put("resumen", reporteService.getResumenAdmin(pEventoId, pAnio, pMes, pDia));
        response.put("graficaVentas", graficaService.getGraficaLineaVentasAdmin(pEventoId, pAnio, pMes, pDia));
        response.put("graficaMetodo", graficaService.getGraficaDineroRecaudadoByMetodoAdmin(pEventoId, pAnio, pMes, pDia));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
