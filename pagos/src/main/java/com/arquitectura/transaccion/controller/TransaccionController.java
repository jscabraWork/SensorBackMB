package com.arquitectura.transaccion.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.service.TransaccionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transacciones")
public class TransaccionController extends CommonController<Transaccion, TransaccionService> {

    /**
     * Verifica si una orden tiene transacciones en proceso (estado 35)
     *
     * @param ordenId ID de la orden a verificar
     * @return ResponseEntity con mensaje indicando si hay transacciones en proceso o no
     */
    @GetMapping("/verificar-transacciones/{ordenId}")
    public ResponseEntity<?> verificarTransaccionEnProceso(@PathVariable Long ordenId) {
        Map<String, Object> response = new HashMap<>();

        // Buscar transacción en estado 35 para la orden dada
        Transaccion transaccion = service.getTransaccionRepetida(35, ordenId);

        if (transaccion != null) {
            response.put("mensaje", "La orden tiene una transacción en proceso");
            response.put("transaccionId", transaccion.getId());
        } else {
            response.put("mensaje", " ");
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/filtro")
    public ResponseEntity<?> getTransaccionesByFiltro(
            @RequestParam(required = false) String numeroDocumento,
                                               @RequestParam(required = false) String correo,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin,
                                               @RequestParam(required = false) Integer estado,
                                               @RequestParam(required = false) Integer metodo,
                                               @RequestParam(required = false) String metodoNombre,
                                               @RequestParam Integer page, @RequestParam Integer size) {
        Map<String, Object> response = new HashMap<>();

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaccion> transacciones = service.findByFiltro(numeroDocumento, correo, fechaInicio, fechaFin, estado, metodo, metodoNombre, pageable);

        response.put("transacciones", transacciones.getContent());
        response.put("total", transacciones.getTotalElements());
        response.put("totalPages", transacciones.getTotalPages());
        response.put("currentPage", transacciones.getNumber());


        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
