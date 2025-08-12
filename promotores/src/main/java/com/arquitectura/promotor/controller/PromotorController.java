package com.arquitectura.promotor.controller;

import com.arquitectura.controller.CommonControllerString;
import com.arquitectura.evento.entity.Evento;
import com.arquitectura.evento.service.EventoService;
import com.arquitectura.promotor.entity.Promotor;
import com.arquitectura.promotor.service.PromotorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/promotores")
public class PromotorController extends CommonControllerString<Promotor, PromotorService> {

    @Autowired
    private EventoService eventoService;

    //Trae el promotor para editar sus eventos relacionados
    @GetMapping("/promotor/{pNumeroDocumento}")
    public ResponseEntity<?> getPromotorById(@PathVariable String pNumeroDocumento){
        Map<String, Object> response = new HashMap<>();
        Promotor promotor = service.findById(pNumeroDocumento);
        if (promotor!=null) {
            response.put("promotor", promotor);
            response.put("eventosAsignados", promotor.getEventos());
            response.put("eventos", eventoService.findByNoEstado(3));

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("message", "Promotor no encontrado");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/asignar-eventos/{numeroDocumento}")
    public ResponseEntity<?> asignarEventos(@PathVariable String numeroDocumento, @RequestBody List<Long> pEventoid) {

        Map<String, Object> response = new HashMap<>();

        Promotor promotor = service.asignarEventos(numeroDocumento, pEventoid);

        response.put("promotor", promotor);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/filtrar")
    public ResponseEntity<?> filtrarPromotores(@RequestParam(required = false) String nombre,
                                               @RequestParam(required = false) String numeroDocumento,
                                               @RequestParam(required = false) String correo) {
        Map<String, Object> response = new HashMap<>();
        List<Promotor> promotores = service.findByFiltro(nombre, numeroDocumento, correo);
        response.put("promotores", promotores);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
