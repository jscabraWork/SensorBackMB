package com.arquitectura.tarifa.controller;

import com.arquitectura.controller.CommonController;
import com.arquitectura.tarifa.entity.Tarifa;
import com.arquitectura.tarifa.service.TarifaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tarifas")
public class TarifaController extends CommonController<Tarifa, TarifaService> {

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Tarifa pE, BindingResult result) {
        if (result.hasErrors()) {
            return validar(result);
        }
        Tarifa entityDB = service.saveKafka(pE);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityDB);
    }

    /**
     * Obtiene todas las tarifas filtradas por estado.
     *
     * @param pEstado Estado por el cual filtrar las tarifas (1 = inactivo, 0 = activo, etc.)
     * @param localidadId eventoId por el cual filtrar las tarifas
     * @return ResponseEntity con la lista de tarifas que coinciden con el estado proporcionado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listar/estado")
    public ResponseEntity<?> getAllByEstadoAndLocalidadId(@RequestParam int pEstado,
                                                          @RequestParam Long localidadId) {
        return new ResponseEntity<>(service.findAllByEstadoAndLocalidadId(pEstado, localidadId), HttpStatus.OK);
    }

    /**
     * Obtiene todas las tarifas filtradas por evento.
     *
     * @param eventoId eventoId por el cual filtrar las tarifas
     * @return ResponseEntity con la lista de las tarifas que coinciden con el estado proporcionado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listar/{eventoId}")
    public ResponseEntity<?> getAllByEventoId(@PathVariable Long eventoId) {
        try {
            return new ResponseEntity<>(service.findAllByEventoId(eventoId), HttpStatus.OK);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }
    }

    /**
     * Actualiza el estado de un dia específico.
     *
     * @param tarifaId ID de la tarifa al que se le cambia el estado
     * @return ResponseEntity con el tarifa actualizado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("estado/{tarifaId}")
    public ResponseEntity<?> updateEstadoTarifa(@PathVariable Long tarifaId,
                                                @RequestParam int estado) {
        try {
            Tarifa tarifaActualizada = service.actualizarEstado(tarifaId, estado);
            return ResponseEntity.ok(tarifaActualizada);
        } catch (RuntimeException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", ex.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Actualiza un dia específico.
     *
     * @param pId ID de la tarifa a actualizar
     * @param tarifa datos de la tarifa actualizada
     * @return ResponseEntity con la tarifa actualizada
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/actualizar/{pId}")
    public ResponseEntity<?> updateTarifa(@PathVariable Long pId, @RequestBody Tarifa tarifa) {
        Tarifa tarifaActualizada = service.actualizar(pId, tarifa);
        if(tarifaActualizada == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "Tarifa no encontrada con ID: " + pId);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        return ResponseEntity.ok(tarifaActualizada);
    }

    /**
     * Borra una tarifa en específico.
     *
     * @param tarifaId ID de la tarifa a borrar
     * @return ResponseEntity sin contenido indicando el borrado
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/borrar/{tarifaId}")
    public ResponseEntity<?> borrar(@PathVariable Long tarifaId) {
        try {
            service.deleteById(tarifaId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("mensaje", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("mensaje", e.getMessage()));
        }
    }

    /**
     * Valida si una tarifa tiene tickets asociados.
     *
     * @param pId ID de la tarifa
     * @return ResponseEntity con un booleano , true = tiene tickts asociados , false = no tiene tickets asociados
     */
    @GetMapping("/{pId}/tiene-tickets")
    public ResponseEntity<Boolean> tieneTicketsAsociados(@PathVariable Long pId) {
        Boolean tieneTickets = service.tieneTicketsAsociados(pId);
        return ResponseEntity.ok(tieneTickets);
    }

    @GetMapping("/buscar/{localidadId}")
    public ResponseEntity<?> getAllTarifasByLocalidadId(@PathVariable Long localidadId) {
        List<Tarifa> tarifas = service.findAllByLocalidadId(localidadId);
        return ResponseEntity.ok(tarifas);
    }




}
