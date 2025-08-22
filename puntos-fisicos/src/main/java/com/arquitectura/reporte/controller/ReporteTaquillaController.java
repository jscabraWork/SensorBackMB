package com.arquitectura.reporte.controller;

import com.arquitectura.reporte.service.ReporteTaquillaService;
import com.arquitectura.reporte.view.VentaTaquillaView;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
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
@RequestMapping("/reporte")
public class ReporteTaquillaController {

    @Autowired
    private ReporteTaquillaService service;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/{pEventoId}")
    public ResponseEntity<?> getVentasTaquillaByEventoId(@PathVariable Long pEventoId){
        List<VentaTaquillaView> ventas = service.findByEventoId(pEventoId);
        return new ResponseEntity<>(ventas, HttpStatus.OK);
    }

    @GetMapping("/{pEventoId}/taquilla/{pNumeroDocumento}")
    public ResponseEntity<?> getVentasByEventoIdAndTaquilla(@PathVariable Long pEventoId, @PathVariable String pNumeroDocumento){
        VentaTaquillaView ventas = service.findByDocumentoAndEventoId(pNumeroDocumento, pEventoId);
        List<Ticket> tickets= ticketService.findByEventoIdAndPuntoNumeroDocumentoAndEstado(pEventoId, pNumeroDocumento, 1);

        Map<String, Object> response = new HashMap<>();
        response.put("resumen", ventas);
        response.put("ventas", tickets);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
