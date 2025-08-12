package com.arquitectura.reserva.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.reserva.entity.Reserva;
import com.arquitectura.reserva.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservas")
public class ReservaController extends CommonController<Reserva, ReservaService> {

    @PostMapping("/crear")
    public ResponseEntity<?> crear(@RequestBody Reserva reserva, @RequestParam Long pLocalidadId, @RequestParam String pPromotorId) {
        Map<String, Object> response = new HashMap<>();
        Reserva reservaNew = service.crear(reserva, pLocalidadId, pPromotorId);
        response.put("reserva", reservaNew);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/activas")
    public ResponseEntity<?> findActivasByEventoAndPromotor(@RequestParam Long pEventoId, @RequestParam String pPromotorId) {
       Map<String, Object> response = new HashMap<>();
       List<Reserva> reservas = service.findByPromotorEventoAndEstado(pEventoId, pPromotorId, true);
       response.put("reservas", reservas);
       return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
