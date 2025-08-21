package com.arquitectura.reporte.controller;

import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.reporte.service.ReportePromotorService;
import com.arquitectura.reporte.view.VentaPromorView;
import com.arquitectura.ticket.entity.Ticket;
import com.arquitectura.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reporte")
public class ReportePromotorController {

    @Autowired
    private ReportePromotorService service;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/{pEventoId}")
    public ResponseEntity<?> getVentasPromotorByEventoId(@PathVariable Long pEventoId){
        List<VentaPromorView> ventas = service.findByEventoId(pEventoId);
        return new ResponseEntity<>(ventas, HttpStatus.OK);
    }

    @GetMapping("/{pEventoId}/promotor/{pNumeroDocumento}")
    public ResponseEntity<?> getVentasByEventoIdAndPromotor(@PathVariable Long pEventoId, @PathVariable String pNumeroDocumento){
        VentaPromorView ventas = service.findByDocumentoAndEventoId(pNumeroDocumento, pEventoId);
        List<Ticket> tickets= ticketService.findVentasByPromotorAndEvento(pEventoId, pNumeroDocumento);

        Map<String, Object> response = new HashMap<>();
        response.put("resumen", ventas);
        response.put("ventas", tickets);
        return new ResponseEntity<>(ventas, HttpStatus.OK);
    }

}
