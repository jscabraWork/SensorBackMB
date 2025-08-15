package com.arquitectura.transaccion.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.service.TransaccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/transaccion")
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

}
