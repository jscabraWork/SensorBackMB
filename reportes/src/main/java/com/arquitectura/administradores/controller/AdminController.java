package com.arquitectura.administradores.controller;

import com.arquitectura.evento.entity.Evento;
import com.arquitectura.organizador.service.ReporteService;
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
    private ReporteService reporteService;

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
        response.put("resumen", reporteService.getResumenAdmin(pEventoId, pAnio, pMes, pDia));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
